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
package org.grouplens.lenskit.knn.model;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.cursors.LongCursor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Factory wrapping initialization logic necessary for
 * instantiating an {@link ItemItemBuildContext}.
 */
public class ItemItemBuildContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(ItemItemBuildContextFactory.class);

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
     *
     * @return a new ItemItemBuildContext.
     */
    public ItemItemBuildContext buildContext() {
        logger.info("constructing build context");
        logger.debug("using normalizer {}", normalizer);
        logger.debug("using summarizer {}", userSummarizer);


        LongCollection ilist = Cursors.makeList(dao.getItems());
        LongSortedSet items = new LongSortedArraySet(ilist);

        logger.debug("Building item data");
        Long2ObjectMap<Long2DoubleMap> itemData = buildItemRatings(items);
        // finalize the item data into vectors
        Long2ObjectMap<SparseVector> itemRatings = new Long2ObjectOpenHashMap<SparseVector>(itemData.size());
        for (Long2ObjectMap.Entry<Long2DoubleMap> entry : CollectionUtils.fast(itemData.long2ObjectEntrySet())) {
            Long2DoubleMap ratings = entry.getValue();
            SparseVector v = new ImmutableSparseVector(ratings);
            assert v.size() == ratings.size();
            itemRatings.put(entry.getLongKey(), v);
            entry.setValue(null);          // clear the array so GC can free
        }
        assert itemRatings.size() == itemData.size();

        return new ItemItemBuildContext(items, itemRatings);
    }

    /**
     * Transpose the user matrix so we have a matrix of item ids
     * to ratings.
     *
     * @param items A SortedSet of item ids to be mapped to ratings.
     * @return a Long2ObjectMap<Long2DoubleMap> encoding a matrix
     *         of item ids to (userId: rating) pairs.
     */
    private Long2ObjectMap<Long2DoubleMap> buildItemRatings(LongSortedSet items) {
        final int nitems = items.size();

        // Create and initialize the transposed array to collect item vector data
        Long2ObjectMap<Long2DoubleMap> workMatrix =
                new Long2ObjectOpenHashMap<Long2DoubleMap>(nitems);
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long iid = iter.nextLong();
            workMatrix.put(iid, new Long2DoubleOpenHashMap(20));
        }

        LongCursor userCursor = dao.getUsers();
        try {
            while (userCursor.hasNext()) {
                long uid = userCursor.nextLong();
                SparseVector summary = userSummarizer.summarize(dao.getUserHistory(uid));
                MutableSparseVector normed = summary.mutableCopy();
                normalizer.normalize(uid, summary, normed);

                for (VectorEntry rating : normed.fast()) {
                    final long item = rating.getKey();
                    // get the item's rating vector
                    Long2DoubleMap ivect = workMatrix.get(item);
                    ivect.put(uid, rating.getValue());
                }
            }
        } finally {
            userCursor.close();
        }

        return workMatrix;
    }

}
