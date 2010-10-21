/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.item;

import java.util.Map;

import javax.annotation.Nullable;

import org.grouplens.reflens.Normalizer;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.SymmetricBinaryFunction;
import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.RatingNormalization;
import org.grouplens.reflens.item.params.ThreadCount;
import org.grouplens.reflens.util.Cursor;
import org.grouplens.reflens.util.DataSource;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;
import org.grouplens.reflens.util.parallel.IntWorker;
import org.grouplens.reflens.util.parallel.IntegerTaskQueue;
import org.grouplens.reflens.util.parallel.IteratorTaskQueue;
import org.grouplens.reflens.util.parallel.ObjectWorker;
import org.grouplens.reflens.util.parallel.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ParallelItemItemRecommenderBuilder implements
		RecommenderBuilder<Integer, Integer> {
	
	private static final Logger logger = LoggerFactory.getLogger(ParallelItemItemRecommenderBuilder.class);

	private Provider<Indexer<Integer>> indexProvider;
	private Provider<Map<Integer,Double>> itemMapProvider;
	private SimilarityMatrixBuilderFactory matrixFactory;
	private Normalizer<Integer, Map<Integer,Double>> ratingNormalizer;
	private Similarity<Map<Integer,Double>> itemSimilarity;
	private final int threadCount;

	@Inject
	public ParallelItemItemRecommenderBuilder(
			Provider<Indexer<Integer>> indexProvider,
			Provider<Map<Integer,Double>> itemMapProvider,
			SimilarityMatrixBuilderFactory matrixFactory,
			@ThreadCount int threadCount,
			@ItemSimilarity Similarity<Map<Integer,Double>> itemSimilarity,
			@Nullable @RatingNormalization Normalizer<Integer,Map<Integer,Double>> ratingNormalizer) {
		this.indexProvider = indexProvider;
		this.itemMapProvider = itemMapProvider;
		this.matrixFactory = matrixFactory;
		this.ratingNormalizer = ratingNormalizer;
		this.itemSimilarity = itemSimilarity;
		this.threadCount = threadCount;
	}
	
	private Index<Integer> indexItems(DataSource<UserRatingProfile<Integer, Integer>> data) {
		Indexer<Integer> indexer = indexProvider.get();
		Cursor<UserRatingProfile<Integer, Integer>> cursor = data.cursor();
		try {
			for (UserRatingProfile<Integer,Integer> profile: cursor) {
				for (Integer item: profile.getRatings().keySet()) {
					indexer.internObject(item);
				}
			}
		} finally {
			cursor.close();
		}
		return indexer;
	}
	
	@Override
	public ItemItemRecommender<Integer,Integer> build(DataSource<UserRatingProfile<Integer,Integer>> data) {
		logger.info("Building model for {} ratings with {} threads", data.getRowCount(), threadCount);
		logger.debug("Indexing items");
		Index<Integer> itemIndex = indexItems(data);
		logger.debug("Normalizing and transposing ratings matrix");
		Map<Integer,Double>[] itemRatings = buildItemRatings(itemIndex, data);
		
		// prepare the similarity matrix
		logger.debug("Initializing similarity matrix");
		SimilarityMatrixBuilder builder = matrixFactory.create(itemRatings.length);
		
		WorkerFactory<IntWorker> worker;
		IntegerTaskQueue queue;
		
		// compute the similarity matrix
		if (itemSimilarity instanceof SymmetricBinaryFunction) {
			logger.debug("Computing similarities (symmetric builder)");
			worker = new SymmetricWorkerFactory(itemRatings, builder);
			queue = new IntegerTaskQueue(arithSum(itemRatings.length));
		} else {
			logger.debug("Computing similarities (asymmetric builder)");
			worker = new AsymmetricWorkerFactory(itemRatings, builder);
			queue = new IntegerTaskQueue(itemRatings.length * itemRatings.length);
		}
		
		queue.run(worker, threadCount);
		
		logger.debug("Finalizing recommender model");
		SimilarityMatrix matrix = builder.build();
		ItemItemModel<Integer,Integer> model = new ItemItemModel<Integer,Integer>(itemIndex, matrix);
		return new ItemItemRecommender<Integer,Integer>(model);
	}
	
	static int arithSum(int n) {
		return (n * (n + 1)) >> 1; // bake-in the strength reduction
	}
	
	// private helper method to isolate the unchecked cast warning
	@SuppressWarnings("unchecked")
	private static <K,V> Map<K,V>[] makeMapArray(int n) {
		return new Map[n];
	}
	
	/** 
	 * Transpose the ratings matrix so we have a list of item rating vectors.
	 * @return An array of item rating vectors, mapping user IDs to ratings.
	 */
	private Map<Integer,Double>[] buildItemRatings(final Index<Integer> index, DataSource<UserRatingProfile<Integer,Integer>> data) {
		final Map<Integer,Double>[] itemVectors = makeMapArray(index.getObjectCount());
		for (int i = 0; i < itemVectors.length; i++) {
			itemVectors[i] = itemMapProvider.get();
		}
		
		Cursor<UserRatingProfile<Integer, Integer>> cursor = data.cursor();
		
		try {		
			IteratorTaskQueue.parallelDo(cursor.iterator(), threadCount,
					new WorkerFactory<ObjectWorker<UserRatingProfile<Integer,Integer>>>() {
				@Override
				public ObjectWorker<UserRatingProfile<Integer, Integer>> create(
						Thread owner) {
					return new ObjectWorker<UserRatingProfile<Integer, Integer>>() {
						@Override
						public void doJob(UserRatingProfile<Integer, Integer> profile) {
							Map<Integer,Double> ratings = profile.getRatings();
							if (ratingNormalizer != null)
								ratings = ratingNormalizer.normalize(profile.getUser(), ratings);
							for (Map.Entry<Integer, Double> rating: ratings.entrySet()) {
								int item = rating.getKey();
								int idx = index.getIndex(item);
								assert idx >= 0;
								Map<Integer,Double> v = itemVectors[idx];
								synchronized (v) {
									v.put(profile.getUser(), rating.getValue());
								}
							}
						}
						@Override public void finish() {}

					};
				}
			});
		} finally {
			cursor.close();
		}
		return itemVectors;
	}
	
	private class SymmetricSimWorker implements IntWorker {
		private final Map<Integer,Double>[] itemVectors;
		private final SimilarityMatrixBuilder builder;
		private final SymmetricRowCounter counter;
		
		public SymmetricSimWorker(Map<Integer,Double>[] items, SimilarityMatrixBuilder builder) {
			this.itemVectors = items;
			this.builder = builder;
			counter = new SymmetricRowCounter();
		}

		@Override
		public void doJob(long job) {
			counter.advance(job);
			int row = counter.getRow();
			int col = counter.getColumn();
			if (row == col)
				return;
			
			double sim = itemSimilarity.similarity(itemVectors[row], itemVectors[col]);
			builder.put(row, col, sim);
			builder.put(col, row, sim);
		}

		@Override
		public void finish() {
		}
	}
	
	/**
	 * Counter class for keeping track of rows and columns for symmetric processing.
	 * 
	 * This class maintains the invariant that <var>fullDone</var> is the
	 * number of cells in full rows up until <var>row</var>.
	 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
	 *
	 */
	static class SymmetricRowCounter {
		private int row;
		private int column;
		private long fullDone;
		public SymmetricRowCounter() {
			row = 0;
			fullDone = 0;
		}
		
		public void advance(long job) {
			while (job >= fullDone + row + 1) {
				row += 1;
				fullDone = arithSum(row);
			}
			column = (int) (job - fullDone);
		}
		
		public int getRow() {
			return row;
		}
		
		public int getColumn() {
			return column;
		}
	}
	
	public class SymmetricWorkerFactory implements WorkerFactory<IntWorker> {

		private final Map<Integer, Double>[] itemVector;
		private final SimilarityMatrixBuilder builder;
		
		public SymmetricWorkerFactory(Map<Integer,Double>[] items, SimilarityMatrixBuilder builder) {
			this.itemVector = items;
			this.builder = builder;
		}
		@Override
		public SymmetricSimWorker create(Thread owner) {
			return new SymmetricSimWorker(itemVector, builder);
		}
		
	}
	
	private class AsymmetricSimWorker implements IntWorker {
		private final Map<Integer,Double>[] itemVectors;
		private final SimilarityMatrixBuilder builder;
		
		public AsymmetricSimWorker(Map<Integer,Double>[] items, SimilarityMatrixBuilder builder) {
			this.itemVectors = items;
			this.builder = builder;
		}

		@Override
		public void doJob(long job) {
			int i1 = (int) (job / itemVectors.length);
			int i2 = (int) (job % itemVectors.length);
			if (i1 == i2)
				return;
			double sim = itemSimilarity.similarity(itemVectors[i1], itemVectors[i2]);
			builder.put(i1, i2, sim);
		}

		@Override
		public void finish() {
		}
	}
	
	public class AsymmetricWorkerFactory implements WorkerFactory<IntWorker> {

		private final Map<Integer, Double>[] itemVector;
		private final SimilarityMatrixBuilder builder;
		
		public AsymmetricWorkerFactory(Map<Integer,Double>[] items, SimilarityMatrixBuilder builder) {
			this.itemVector = items;
			this.builder = builder;
		}
		@Override
		public AsymmetricSimWorker create(Thread owner) {
			return new AsymmetricSimWorker(itemVector, builder);
		}
		
	}


}
