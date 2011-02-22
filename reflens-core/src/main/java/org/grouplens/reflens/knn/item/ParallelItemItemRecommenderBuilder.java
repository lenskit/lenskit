/*
 * RefLens, a reference implementation of recommender algorithms.
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

package org.grouplens.reflens.knn.item;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.data.vector.MutableSparseVector;
import org.grouplens.reflens.data.vector.SparseVector;
import org.grouplens.reflens.knn.OptimizableVectorSimilarity;
import org.grouplens.reflens.knn.Similarity;
import org.grouplens.reflens.knn.SimilarityMatrix;
import org.grouplens.reflens.knn.SimilarityMatrixBuilder;
import org.grouplens.reflens.knn.SimilarityMatrixBuilderFactory;
import org.grouplens.reflens.knn.params.ItemSimilarity;
import org.grouplens.reflens.params.BaselinePredictor;
import org.grouplens.reflens.params.ThreadCount;
import org.grouplens.reflens.util.SymmetricBinaryFunction;
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
	private Similarity<? super SparseVector> itemSimilarity;
	@Nullable private final RatingPredictorBuilder baselineBuilder;
	private final int threadCount;
	private Long2ObjectMap<IntSortedSet> userItemMap;
	@Nullable private RatingPredictor baseline = null;

	@Inject
	public ParallelItemItemRecommenderBuilder(
			SimilarityMatrixBuilderFactory matrixFactory,
			@ThreadCount int threadCount,
			@ItemSimilarity OptimizableVectorSimilarity<? super SparseVector> itemSimilarity,
			@Nullable @BaselinePredictor RatingPredictorBuilder baselineBuilder) {
		this.matrixFactory = matrixFactory;
		this.baselineBuilder = baselineBuilder;
		this.itemSimilarity = itemSimilarity;
		this.threadCount = threadCount;
	}
	
	private Index indexItems(RatingDataSource data) {
		userItemMap = new Long2ObjectOpenHashMap<IntSortedSet>();
		Indexer indexer = new Indexer();
		Cursor<UserRatingProfile> cursor = data.getUserRatingProfiles();
		try {
			for (UserRatingProfile profile: cursor) {
				IntSortedSet s = new IntRBTreeSet();
				userItemMap.put(profile.getUser(), s);
				for (Rating r: profile.getRatings()) {
					int idx = indexer.internId(r.getItemId());
					s.add(idx);
				}
			}
		} finally {
			cursor.close();
		}
		return indexer;
	}
	
	@Override
	public ItemItemRecommenderService build(RatingDataSource data) {
		logger.info("Building model with {} threads", threadCount);
		// TODO look in to merging these passes
		baseline = baselineBuilder.build(data);
		logger.debug("Indexing items");
		Index itemIndex = indexItems(data);
		logger.debug("Normalizing and transposing ratings matrix");
		SparseVector[] itemRatings = buildItemRatings(itemIndex, data);
		
		// prepare the similarity matrix
		logger.debug("Initializing similarity matrix");
		SimilarityMatrixBuilder builder = matrixFactory.create(itemRatings.length);
		
		// compute the similarity matrix
		WorkerFactory<IntWorker> worker = new SimilarityWorkerFactory(itemRatings, builder);
		IntegerTaskQueue queue = new IntegerTaskQueue(itemRatings.length);
		
		logger.debug("Computing similarities");
		queue.run(worker, threadCount);
		
		logger.debug("Finalizing recommender model");
		SimilarityMatrix matrix = builder.build();
		ItemItemModel model = new ItemItemModel(itemIndex, baseline, matrix);
		return new ItemItemRecommenderService(model);
	}
	
	static int arithSum(int n) {
		return (n * (n + 1)) >> 1; // bake-in the strength reduction
	}
	static long arithSum(long n) {
		return (n * (n + 1)) >> 1; // bake-in the strength reduction
	}
	
	/** 
	 * Transpose the ratings matrix so we have a list of item rating vectors.
	 * @return An array of item rating vectors, mapping user IDs to ratings.
	 */
	private SparseVector[] buildItemRatings(final Index index, RatingDataSource data) {
		final int nitems = data.getItemCount();
		final Long2DoubleMap[] itemWork = new Long2DoubleMap[nitems];
		for (int i = 0; i < nitems; i++) {
			itemWork[i] = new Long2DoubleOpenHashMap();
		}
		Cursor<UserRatingProfile> cursor = data.getUserRatingProfiles();
		try {
			IteratorTaskQueue.parallelDo(cursor.iterator(), threadCount,
					new WorkerFactory<ObjectWorker<UserRatingProfile>>() {
				@Override
				public ObjectWorker<UserRatingProfile> create(
						Thread owner) {
					return new ObjectWorker<UserRatingProfile>() {
						@Override
						public void doJob(UserRatingProfile profile) {
							Collection<Rating> ratings = profile.getRatings();
							ratings = normalizeUserRatings(profile.getUser(), ratings);
							for (Rating rating: ratings) {
								long item = rating.getItemId();
								int idx = index.getIndex(item);
								assert idx >= 0;
								Long2DoubleMap v = itemWork[idx];
								synchronized (v) {
									v.put(profile.getUser(), rating.getRating());
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
		// Transmogrify the item vector workspace into real rating vectors.
		SparseVector[] itemVectors = new SparseVector[nitems];
		for (int i = 0; i < nitems; i++) {
			itemVectors[i] = new MutableSparseVector(itemWork[i]);
			itemWork[i] = null;
		}
		return itemVectors;
	}
	
	private class SimilarityWorker implements IntWorker {
		private final SparseVector[] itemVectors;
		private final SimilarityMatrixBuilder builder;
		private final boolean symmetric;
		
		public SimilarityWorker(SparseVector[] items, SimilarityMatrixBuilder builder) {
			this.itemVectors = items;
			this.builder = builder;
			this.symmetric = itemSimilarity instanceof SymmetricBinaryFunction;
		}

		@Override
		public void doJob(int job) {
			int row = (int) job;
			int max = symmetric ? row : itemVectors.length;
			
			IntSet candidates = new IntOpenHashSet();
			LongIterator uiter = itemVectors[row].keySet().iterator();
			while (uiter.hasNext()) {
				long u = uiter.nextLong();
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

		private final SparseVector[] itemVector;
		private final SimilarityMatrixBuilder builder;
		
		public SimilarityWorkerFactory(SparseVector[] items, SimilarityMatrixBuilder builder) {
			this.itemVector = items;
			this.builder = builder;
		}
		@Override
		public SimilarityWorker create(Thread owner) {
			return new SimilarityWorker(itemVector, builder);
		}
		
	}
	
	protected Collection<Rating> normalizeUserRatings(long uid, Collection<Rating> ratings) {
		// TODO share this code with ItemItemRecommenderBuilder
		if (baseline == null) return ratings;
		
		SparseVector rmap = Rating.userRatingVector(ratings);
		SparseVector base = baseline.predict(uid, rmap, rmap.keySet());
		Collection<Rating> normed = new ArrayList<Rating>(ratings.size());
		
		for (Rating r: ratings) {
			long iid = r.getItemId();
			double adj = r.getRating() - base.get(iid);
			Rating r2 = new Rating(r.getUserId(), r.getItemId(), adj, r.getTimestamp());
			normed.add(r2);
		}
		return normed;
	}

}
