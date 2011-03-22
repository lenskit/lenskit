/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuilder;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.Indexer;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.RatingDataSource;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.SimilarityMatrix;
import org.grouplens.lenskit.util.IntSortedArraySet;
import org.grouplens.lenskit.util.LongSortedArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Builds item-item recommender engines from data sources.
 *
 * This class takes {@link RatingDataSource}es and builds item-item recommender
 * models from them.  It uses a build strategy and a baseline recommender to do
 * the actual building, constructs an {@link ItemItemModel} containing the
 * resulting recommender model, and finally builds a recommender around it.
 *
 * The recommender engine builder uses an {@link ItemItemRecommenderServiceFactory}
 * to actually construct the recommender engine.  Re-binding that interface
 * allows alternative recommender engines to be used.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemRecommenderBuilder implements RecommenderBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemRecommenderBuilder.class);

    private final @Nonnull ItemItemRecommenderServiceFactory engineFactory;
    private final @Nonnull SimilarityMatrixBuildStrategy similarityStrategy;

    @Inject
    ItemItemRecommenderBuilder(
            SimilarityMatrixBuildStrategy similarityStrategy,
            ItemItemRecommenderServiceFactory engineFactory) {
        this.similarityStrategy = similarityStrategy;
        this.engineFactory = engineFactory;
    }

    @ParametersAreNonnullByDefault
    final class BuildState {
        public final @Nullable RatingPredictor baseline;
        public final Index itemIndex;
        public ArrayList<SparseVector> itemRatings;
        public final @Nullable Long2ObjectMap<IntSortedSet> userItemSets;
        public final int itemCount;

        public BuildState(RatingDataSource data, @Nullable RatingPredictor baseline,
                boolean trackItemSets) {
            this.baseline = baseline;
            Indexer itemIndexer;
            itemIndex = itemIndexer = new Indexer();
            itemRatings = new ArrayList<SparseVector>();

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
         * @todo Fix this method to abstract item collection.
         */
        private void buildItemRatings(Indexer itemIndexer, RatingDataSource data) {
            Cursor<UserRatingProfile> cursor = data.getUserRatingProfiles();
            final boolean collectItems = userItemSets != null;
            ArrayList<Long2DoubleMap> itemWork = new ArrayList<Long2DoubleMap>(100);
            try {
                for (UserRatingProfile user: cursor) {
                    Collection<Rating> ratings = user.getRatings();
                    ratings = normalizeUserRatings(baseline, user.getUser(), ratings);
                    final int nratings = ratings.size();
                    // allocate the array ourselves to avoid an array copy
                    int[] userItemArr = null;
                    IntCollection userItems = null;
                    if (collectItems) {
                        userItemArr = new int[nratings];
                        userItems = IntArrayList.wrap(userItemArr, 0);
                    }
                    for (Rating rating: ratings) {
                        long item = rating.getItemId();
                        int idx = itemIndexer.internId(item);
                        Long2DoubleMap ivect;
                        if (idx >= itemWork.size()) {
                            // it's a new item - add one
                            assert idx == itemWork.size();
                            ivect = new Long2DoubleOpenHashMap();
                            itemWork.add(ivect);
                        } else {
                            ivect = itemWork.get(idx);
                        }
                        ivect.put(user.getUser(), (double) rating.getRating());
                        if (userItems != null)
                            userItems.add(idx);
                    }
                    if (collectItems) {
                        IntSortedSet itemSet = new IntSortedArraySet(userItemArr, 0, userItems.size());
                        userItemSets.put(user.getUser(), itemSet);
                    }
                }
            } finally {
                cursor.close();
            }

            // convert the temporary work array into a real array
            itemRatings = new ArrayList<SparseVector>(itemWork.size());
            ListIterator<Long2DoubleMap> iter = itemWork.listIterator();
            while (iter.hasNext()) {
                Long2DoubleMap ratings = iter.next();
                SparseVector v = new SparseVector(ratings);
                assert v.size() == ratings.size();
                itemRatings.add(v);
                iter.set(null);                // clear the array so GC can free
            }
            assert itemRatings.size() == itemWork.size();
        }
    }

    @Override
    public ItemItemRecommenderService build(RatingDataSource data, @Nullable RatingPredictor baseline) {
        BuildState state = new BuildState(data, baseline, similarityStrategy.needsUserItemSets());

        SimilarityMatrix matrix = similarityStrategy.buildMatrix(state);
        LongSortedArraySet items = new LongSortedArraySet(state.itemIndex.getIds());
        ItemItemModel model = new ItemItemModel(state.itemIndex, state.baseline, matrix, items);
        return engineFactory.create(model);
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

        SparseVector rmap = Ratings.userRatingVector(ratings);
        SparseVector base = baseline.predict(uid, rmap, rmap.keySet());
        Collection<Rating> normed = new ArrayList<Rating>(ratings.size());

        for (Rating r: ratings) {
            long iid = r.getItemId();
            double adj = r.getRating() - base.get(iid);
            Rating r2 = new SimpleRating(r.getUserId(), r.getItemId(), adj, r.getTimestamp());
            normed.add(r2);
        }
        return normed;
    }
}
