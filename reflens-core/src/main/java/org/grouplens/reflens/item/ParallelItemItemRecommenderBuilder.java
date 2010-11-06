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

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Map;

import javax.annotation.Nullable;

import org.grouplens.reflens.Normalizer;
import org.grouplens.reflens.OptimizableMapSimilarity;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.SymmetricBinaryFunction;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.DataSet;
import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.RatingNormalization;
import org.grouplens.reflens.item.params.ThreadCount;
import org.grouplens.reflens.util.ProgressReporter;
import org.grouplens.reflens.util.ProgressReporterFactory;
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

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ParallelItemItemRecommenderBuilder implements RecommenderBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(ParallelItemItemRecommenderBuilder.class);

	private SimilarityMatrixBuilderFactory matrixFactory;
	@Nullable
	private Normalizer<Long, Map<Long,Double>> ratingNormalizer;
	private Similarity<Long2DoubleMap> itemSimilarity;
	@Nullable
	private final ProgressReporterFactory progressFactory;
	private final int threadCount;
	private Long2ObjectMap<IntSortedSet> userItemMap;

	@Inject
	public ParallelItemItemRecommenderBuilder(
			SimilarityMatrixBuilderFactory matrixFactory,
			@Nullable ProgressReporterFactory progressFactory,
			@ThreadCount int threadCount,
			@ItemSimilarity OptimizableMapSimilarity<Long,Double, Long2DoubleMap> itemSimilarity,
			@Nullable @RatingNormalization Normalizer<Long,Map<Long,Double>> ratingNormalizer) {
		this.progressFactory = progressFactory;
		this.matrixFactory = matrixFactory;
		this.ratingNormalizer = ratingNormalizer;
		this.itemSimilarity = itemSimilarity;
		this.threadCount = threadCount;
	}
	
	private Index indexItems(DataSet<UserRatingProfile> data) {
		userItemMap = new Long2ObjectOpenHashMap<IntSortedSet>();
		Indexer indexer = new Indexer();
		Cursor<UserRatingProfile> cursor = data.cursor();
		try {
			for (UserRatingProfile profile: cursor) {
				IntSortedSet s = new IntRBTreeSet();
				userItemMap.put(profile.getUser(), s);
				for (long item: profile.getRatings().keySet()) {
					int idx = indexer.internId(item);
					s.add(idx);
				}
			}
		} finally {
			cursor.close();
		}
		return indexer;
	}
	
	@Override
	public ItemItemRecommender build(DataSet<UserRatingProfile> data) {
		logger.info("Building model for {} ratings with {} threads", data.getRowCount(), threadCount);
		logger.debug("Indexing items");
		Index itemIndex = indexItems(data);
		logger.debug("Normalizing and transposing ratings matrix");
		Long2DoubleMap[] itemRatings = buildItemRatings(itemIndex, data);
		
		// prepare the similarity matrix
		logger.debug("Initializing similarity matrix");
		SimilarityMatrixBuilder builder = matrixFactory.create(itemRatings.length);
		
		// compute the similarity matrix
		ProgressReporter rep = null;
		if (progressFactory != null)
			rep = progressFactory.create("similarity");
		WorkerFactory<IntWorker> worker = new SimilarityWorkerFactory(itemRatings, builder);
		if (itemSimilarity instanceof SymmetricBinaryFunction) {
			if (rep != null) {
				rep = new ExpandedProgressReporter(rep);
			}
		}
		IntegerTaskQueue queue = new IntegerTaskQueue(rep, itemRatings.length);
		
		logger.debug("Computing similarities");
		queue.run(worker, threadCount);
		
		logger.debug("Finalizing recommender model");
		SimilarityMatrix matrix = builder.build();
		ItemItemModel model = new ItemItemModel(itemIndex, matrix);
		return new ItemItemRecommender(model);
	}
	
	static int arithSum(int n) {
		return (n * (n + 1)) >> 1; // bake-in the strength reduction
	}
	static long arithSum(long n) {
		return (n * (n + 1)) >> 1; // bake-in the strength reduction
	}
	
	static class ExpandedProgressReporter implements ProgressReporter {
		private final ProgressReporter base;
		public ExpandedProgressReporter(ProgressReporter base) {
			this.base = base;
		}
		@Override
		public void finish() {
			base.finish();
		}
		@Override
		public void setProgress(long current) {
			base.setProgress(arithSum(current));
		}
		@Override
		public void setProgress(long current, long total) {
			base.setProgress(arithSum(current), arithSum(total));
		}
		@Override
		public void setTotal(long total) {
			base.setTotal(arithSum(total));
		}
	}
	
	/** 
	 * Transpose the ratings matrix so we have a list of item rating vectors.
	 * @return An array of item rating vectors, mapping user IDs to ratings.
	 */
	private Long2DoubleMap[] buildItemRatings(final Index index, DataSet<UserRatingProfile> data) {
		final Long2DoubleMap[] itemVectors = new Long2DoubleMap[data.getRowCount()];
		for (int i = 0; i < itemVectors.length; i++) {
			itemVectors[i] = new Long2DoubleOpenHashMap();
		}
		
		Cursor<UserRatingProfile> cursor = data.cursor();
		
		try {		
			ProgressReporter progress = null;
			if (progressFactory != null) {
				progress = progressFactory.create("normalize");
				if (progress != null)
					progress.setTotal(data.getRowCount());
			}
			IteratorTaskQueue.parallelDo(progress, cursor.iterator(), threadCount,
					new WorkerFactory<ObjectWorker<UserRatingProfile>>() {
				@Override
				public ObjectWorker<UserRatingProfile> create(
						Thread owner) {
					return new ObjectWorker<UserRatingProfile>() {
						@Override
						public void doJob(UserRatingProfile profile) {
							Map<Long,Double> ratings = profile.getRatings();
							if (ratingNormalizer != null)
								ratings = ratingNormalizer.normalize(profile.getUser(), ratings);
							for (Map.Entry<Long, Double> rating: ratings.entrySet()) {
								long item = rating.getKey();
								int idx = index.getIndex(item);
								assert idx >= 0;
								Long2DoubleMap v = itemVectors[idx];
								synchronized (v) {
									v.put(profile.getUser(), rating.getValue().doubleValue());
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
	
	private class SimilarityWorker implements IntWorker {
		private final Long2DoubleMap[] itemVectors;
		private final SimilarityMatrixBuilder builder;
		private final boolean symmetric;
		
		public SimilarityWorker(Long2DoubleMap[] items, SimilarityMatrixBuilder builder) {
			this.itemVectors = items;
			this.builder = builder;
			this.symmetric = itemSimilarity instanceof SymmetricBinaryFunction;
		}

		@Override
		public void doJob(int job) {
			int row = (int) job;
			int max = symmetric ? row : itemVectors.length;
			
			IntSet candidates = new IntOpenHashSet();
			for (long u: itemVectors[row].keySet()) {
				IntIterator is = userItemMap.get(u).iterator();
				while (is.hasNext()) {
					int i = is.nextInt();
					if (i >= max) break;
					candidates.add(i);
				}
			}
			
			IntIterator iter = candidates.iterator();
			while (iter.hasNext()) {
				int col = iter.nextInt();
				
				double sim = itemSimilarity.similarity(itemVectors[row], itemVectors[col]);
				builder.put(row, col, sim);
				if (symmetric)
					builder.put(col, row, sim);
			}
		}

		@Override
		public void finish() {
		}
	}
	
	public class SimilarityWorkerFactory implements WorkerFactory<IntWorker> {

		private final Long2DoubleMap[] itemVector;
		private final SimilarityMatrixBuilder builder;
		
		public SimilarityWorkerFactory(Long2DoubleMap[] items, SimilarityMatrixBuilder builder) {
			this.itemVector = items;
			this.builder = builder;
		}
		@Override
		public SimilarityWorker create(Thread owner) {
			return new SimilarityWorker(itemVector, builder);
		}
		
	}

}
