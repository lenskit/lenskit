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

package org.grouplens.reflens.knn;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.RecommenderEngineBuilder;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.knn.params.ItemSimilarity;
import org.grouplens.reflens.params.BaselinePredictor;
import org.grouplens.reflens.util.CollectionUtils;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;
import org.grouplens.reflens.util.SymmetricBinaryFunction;

import com.google.inject.Inject;

public class ItemItemRecommenderBuilder implements RecommenderEngineBuilder {
	
	private final SimilarityMatrixBuilderFactory matrixFactory;
	// TODO Make this Similarity<? super Long2DoubleMap> if we can w/ Guice
	private final Similarity<Long2DoubleMap> itemSimilarity;
	@Nullable private final RatingPredictorBuilder baselineBuilder;
	@Nullable private RatingPredictor baseline;

	@Inject
	ItemItemRecommenderBuilder(
			SimilarityMatrixBuilderFactory matrixFactory,
			@ItemSimilarity Similarity<Long2DoubleMap> itemSimilarity,
			@Nullable @BaselinePredictor RatingPredictorBuilder baselineBuilder) {
		this.matrixFactory = matrixFactory;
		this.itemSimilarity = itemSimilarity;
		this.baselineBuilder = baselineBuilder;
	}
	
	@Override
	public ItemItemRecommender build(RatingDataSource data) {
		Indexer indexer = new Indexer();
		if (baselineBuilder != null)
			baseline = baselineBuilder.build(data);
		
		List<Long2DoubleMap> itemRatings = buildItemRatings(indexer, data);
		
		// prepare the similarity matrix
		SimilarityMatrixBuilder builder = matrixFactory.create(itemRatings.size());
		
		// compute the similarity matrix
		if (itemSimilarity instanceof SymmetricBinaryFunction) {
			// we can compute equivalent symmetries at the same time
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = i+1; j < itemRatings.size(); j++) {
					double sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0) {
						builder.put(i, j, sim);
						builder.put(j, i, sim);
					}
				}
			}
		} else {
			// less efficient route
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = 0; j < itemRatings.size(); j++) {
					double sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0)
						builder.put(i, j, sim);
				}
			}
		}
		
		SimilarityMatrix matrix = builder.build();
		ItemItemModel model = new ItemItemModel(indexer, baseline, matrix);
		return new ItemItemRecommender(model);
	}
	
	protected RatingPredictor getBaseline() {
		return baseline;
	}
	
	/** 
	 * Transpose the ratings matrix so we have a list of item rating vectors.
	 * @return
	 */
	private List<Long2DoubleMap> buildItemRatings(Indexer indexer, RatingDataSource data) {
		ArrayList<Long2DoubleMap> itemVectors = new ArrayList<Long2DoubleMap>();
		Cursor<UserRatingProfile> cursor = data.getUserRatingProfiles();
		try {
			for (UserRatingProfile user: cursor) {
				Collection<Rating> ratings = user.getRatings();
				ratings = normalizeUserRatings(user.getUser(), ratings);
				for (Rating rating: ratings) {
					long item = rating.getItemId();
					int idx = indexer.internId(item);
					if (idx >= itemVectors.size()) {
						// it's a new item - add one
						assert idx == itemVectors.size();
						itemVectors.add(new Long2DoubleOpenHashMap());
					}
					itemVectors.get(idx).put(user.getUser(), (double) rating.getRating());
				}
			}
		} finally {
			cursor.close();
		}
		itemVectors.trimToSize();
		return itemVectors;
	}
	
	protected Collection<Rating> normalizeUserRatings(long uid, Collection<Rating> ratings) {
		if (baseline == null) return ratings;
		
		Long2DoubleMap rmap = new Long2DoubleOpenHashMap(ratings.size());
		for (Rating r: ratings) {
			rmap.put(r.getItemId(), r.getRating());
		}
		Long2DoubleMap base = CollectionUtils.getFastMap(
				baseline.predict(uid, rmap, rmap.keySet()));
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
