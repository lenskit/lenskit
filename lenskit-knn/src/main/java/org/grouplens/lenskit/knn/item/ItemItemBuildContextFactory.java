/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Factory wrapping initialization logic necessary for
 * instantiating an {@link ItemItemBuildContext}.
 */
public class ItemItemBuildContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelProvider.class);

    private final DataAccessObject dao;
    private final UserVectorNormalizer normalizer;
    private final UserHistorySummarizer userSummarizer;

    @Inject
    public ItemItemBuildContextFactory(DataAccessObject dao,
                                       UserVectorNormalizer normalizer,
                                       UserHistorySummarizer userSummarizer) {
        this.dao = dao;
        this.normalizer = normalizer;
        this.userSummarizer = userSummarizer;
    }

    /**
     * Constructs and returns a new ItemItemBuildContext.
     * @param buildStrategy The ModelBuildStrategy that the build context
     *                      will be used by.
     * @return a new ItemItemBuildContext.
     */
    public ItemItemBuildContext buildContext(ModelBuildStrategy buildStrategy) {
        logger.info("constructing build context");
        logger.debug("using normalizer {}", normalizer);
        logger.debug("using summarizer {}", userSummarizer);


        LongCollection ilist = Cursors.makeList(dao.getItems());
        LongSortedSet items = new LongSortedArraySet(ilist);

        Long2ObjectMap<LongSortedSet> userItemSets;
        if (buildStrategy.needsUserItemSets()) {
            userItemSets = new Long2ObjectOpenHashMap<LongSortedSet>(items.size());
        } else {
            userItemSets = null;
        }

        logger.debug("Building item data");
        Long2ObjectMap<Long2DoubleMap> itemData = buildItemRatings(items, userItemSets);
        // finalize the item data into vectors
        Long2ObjectMap<SparseVector> itemRatings = new Long2ObjectOpenHashMap<SparseVector>(itemData.size());
        for (Long2ObjectMap.Entry<Long2DoubleMap> entry: CollectionUtils.fast(itemData.long2ObjectEntrySet())) {
            Long2DoubleMap ratings = entry.getValue();
            SparseVector v = new ImmutableSparseVector(ratings);
            assert v.size() == ratings.size();
            itemRatings.put(entry.getLongKey(), v);
            entry.setValue(null);          // clear the array so GC can free
        }
        assert itemRatings.size() == itemData.size();

        return new ItemItemBuildContext(items, itemRatings, userItemSets);
    }

    /**
     * Transpose the user matrix so we have a matrix of item ids
     * to ratings. If the parameter userItemSets is not null, modify
     * it in place to map user ids to rated items.
     * @param items A SortedSet of item ids to be mapped to ratings.
     * @param userItemSets If not null, an empty structure to be
     *                     modified to map user ids to rated items.
     * @return a Long2ObjectMap<Long2DoubleMap> encoding a matrix
     *          of item ids to (userId: rating) pairs. If userItemSets
     *          is not null, it is modified in place to hold a matrix
     *          of user ids to item ids the user has rated.
     * TODO: Fix this method to abstract item collection.
     */
    private Long2ObjectMap<Long2DoubleMap>
    buildItemRatings(LongSortedSet items, @Nullable Long2ObjectMap<LongSortedSet> userItemSets) {
        final int nitems = items.size();

        // Create and initialize the transposed array to collect user
        Long2ObjectMap<Long2DoubleMap> workMatrix =
                new Long2ObjectOpenHashMap<Long2DoubleMap>(nitems);
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long iid = iter.nextLong();
            workMatrix.put(iid, new Long2DoubleOpenHashMap(20));
        }

        for (long uid : dao.getUsers()) {
            SparseVector summary = userSummarizer.summarize(dao.getUserHistory(uid));
            MutableSparseVector normed = summary.mutableCopy();
            normalizer.normalize(uid, summary, normed);
            final int nratings = summary.size();

            // allocate the array ourselves to avoid an array copy
            long[] userItemArr = null;
            LongCollection userItems = null;
            if (userItemSets != null) {
                // we are collecting user item sets
                userItemArr = new long[nratings];
                userItems = LongArrayList.wrap(userItemArr, 0);
            }

            for (VectorEntry rating: normed.fast()) {
                final long item = rating.getKey();
                // get the item's rating vector
                Long2DoubleMap ivect = workMatrix.get(item);
                ivect.put(uid, rating.getValue());
                if (userItems != null)
                    userItems.add(item);
            }
            if (userItems != null) {
                // collected user item sets, finalize that
                LongSortedSet itemSet = new LongSortedArraySet(userItemArr, 0, userItems.size());
                userItemSets.put(uid, itemSet);
            }
        }

        return workMatrix;
    }

}
