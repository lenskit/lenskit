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

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.RecommenderEngineBuilder;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.params.BaselinePredictor;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ItemItemRecommenderBuilder implements RecommenderEngineBuilder {
	private static final Logger logger = LoggerFactory.getLogger(ItemItemRecommenderBuilder.class);
	
	@Nullable private final RatingPredictorBuilder baselineBuilder;
	private final SimilarityMatrixBuildStrategy similarityStrategy;

	// TODO Make the similarity Similarity<? super RatingVector> if we can w/ Guice
	@Inject
	ItemItemRecommenderBuilder(
			SimilarityMatrixBuildStrategy similarityStrategy,
			@Nullable @BaselinePredictor RatingPredictorBuilder baselineBuilder) {
		this.similarityStrategy = similarityStrategy;
		this.baselineBuilder = baselineBuilder;
	}
	
	final class BuildState {
		public final RatingPredictor baseline;
		public final Index itemIndex;
		public final ArrayList<RatingVector> itemRatings;
		public final Long2ObjectMap<IntSortedSet> userItemSets;
		public final int itemCount;
		
		public BuildState(RatingDataSource data, boolean trackItemSets) {
			baseline = baselineBuilder == null ? null : baselineBuilder.build(data);
			Indexer itemIndexer;
			itemIndex = itemIndexer = new Indexer();
			itemRatings = new ArrayList<RatingVector>();
			
			if (trackItemSets)
				userItemSets = new Long2ObjectOpenHashMap<IntSortedSet>();
			else
				userItemSets = null;
			
			logger.debug("Pre-processing ratings");
			buildItemRatings(itemIndexer, data);
			itemCount = itemRatings.size();
		}
		
		/** 
		 * Transpose the ratings matrix so we have a list of item rating vectors.
		 * @return
		 */
		private void buildItemRatings(Indexer itemIndexer, RatingDataSource data) {
			Cursor<UserRatingProfile> cursor = data.getUserRatingProfiles();
			try {
				for (UserRatingProfile user: cursor) {
					Collection<Rating> ratings = user.getRatings();
					ratings = normalizeUserRatings(baseline, user.getUser(), ratings);
					IntSortedSet userItems = null;
					if (userItemSets != null) {
						userItems = new IntAVLTreeSet();
						userItemSets.put(user.getUser(), userItems);
					}
					for (Rating rating: ratings) {
						long item = rating.getItemId();
						int idx = itemIndexer.internId(item);
						RatingVector ivect;
						if (idx >= itemRatings.size()) {
							// it's a new item - add one
							assert idx == itemRatings.size();
							ivect = new RatingVector();
							itemRatings.add(ivect);
						} else {
							ivect = itemRatings.get(idx);
						}
						ivect.put(user.getUser(), (double) rating.getRating());
						if (userItems != null)
							userItems.add(idx);
					}
				}
			} finally {
				cursor.close();
			}
			itemRatings.trimToSize();
		}
	}
	
	@Override
	public ItemItemRecommender build(RatingDataSource data) {
		BuildState state = new BuildState(data, similarityStrategy.needsUserItemSets());
		
		SimilarityMatrix matrix = similarityStrategy.buildMatrix(state);
		ItemItemModel model = new ItemItemModel(state.itemIndex, state.baseline, matrix);
		return new ItemItemRecommender(model);
	}
	
	/**
	 * Normalize a user's ratings.  This method is called on each user's ratings
	 * prior to using the ratings to learn item similarities.  Deriving
	 * classes can customize the normalization method.
	 * @param baseline The baseline predictor for this model build.
	 * @param uid The user ID.
	 * @param ratings The user's ratings.
	 * @return A normalized version of the user's ratings.
	 */
	protected Collection<Rating> normalizeUserRatings(@Nullable RatingPredictor baseline, long uid, Collection<Rating> ratings) {
		if (baseline == null) return ratings;
		
		RatingVector rmap = RatingVector.userRatingVector(ratings);
		RatingVector base = baseline.predict(uid, rmap, rmap.idSet());
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
