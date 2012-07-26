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
import org.grouplens.grapht.annotation.Transient;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Build an item-item CF model from rating data.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@NotThreadSafe
public class ItemItemModelProvider implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelProvider.class);

    private final ItemSimilarity itemSimilarity;

    private final UserVectorNormalizer normalizer;
    private final UserHistorySummarizer userSummarizer;
    private final SimilarityMatrixAccumulatorFactory simMatrixAccumulatorFactory;

    private final DataAccessObject dao;

    @Inject
    public ItemItemModelProvider(@Transient DataAccessObject dao,
                                 ItemSimilarity similarity,
                                 UserVectorNormalizer normalizer,
                                 UserHistorySummarizer sum,
                                 SimilarityMatrixAccumulatorFactory simMatrixAccumulatorFactory) {
        this.dao = dao;
        this.normalizer = normalizer;
        itemSimilarity = similarity;
        userSummarizer = sum;
        this.simMatrixAccumulatorFactory = simMatrixAccumulatorFactory;
    }

    @Override
    public SimilarityMatrixModel get() {
        logger.debug("building item-item model");
        logger.debug("using normalizer {}", normalizer);
        logger.debug("using summarizer {}", userSummarizer);
        ItemItemModelBuildStrategy similarityStrategy = createBuildStrategy(itemSimilarity);

        LongArrayList ilist = Cursors.makeList(dao.getItems());
        LongSortedSet items = new LongSortedArraySet(ilist);

        Long2ObjectMap<LongSortedSet> userItemSets;
        if (similarityStrategy.needsUserItemSets())
            userItemSets = new Long2ObjectOpenHashMap<LongSortedSet>(items.size());
        else
            userItemSets = null;

        logger.debug("Building item data");
        Long2ObjectMap<Long2DoubleMap> itemData =
                buildItemRatings(items, userItemSets);
        // finalize the item data into vectors
        Long2ObjectMap<SparseVector> itemRatings =
                new Long2ObjectOpenHashMap<SparseVector>(itemData.size());
        for (Long2ObjectMap.Entry<Long2DoubleMap> entry: CollectionUtils.fast(itemData.long2ObjectEntrySet())) {
            Long2DoubleMap ratings = entry.getValue();
            SparseVector v = new ImmutableSparseVector(ratings);
            assert v.size() == ratings.size();
            itemRatings.put(entry.getLongKey(), v);
            entry.setValue(null);          // clear the array so GC can free
        }
        assert itemRatings.size() == itemData.size();

        ItemItemBuildContext context = new ItemItemBuildContext(items, itemRatings, userItemSets);
        SimilarityMatrixAccumulator accumulator = simMatrixAccumulatorFactory.create(items);

        similarityStrategy.buildMatrix(context, accumulator);

        return accumulator.build();
    }

    /**
     * Transpose the user matrix so we have a list of item
     * rating vectors.
     * @todo Fix this method to abstract item collection.
     * @todo Review and document this method.
     */
    private Long2ObjectMap<Long2DoubleMap>
    buildItemRatings(LongSortedSet items, Long2ObjectMap<LongSortedSet> userItemSets) {
        final int nitems = items.size();

        // Create and initialize the transposed array to collect user
        Long2ObjectMap<Long2DoubleMap> workMatrix =
                new Long2ObjectOpenHashMap<Long2DoubleMap>(nitems);
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long iid = iter.nextLong();
            workMatrix.put(iid, new Long2DoubleOpenHashMap(20));
        }

        Cursor<? extends UserHistory<? extends Event>> histories =
                dao.getUserHistories(userSummarizer.eventTypeWanted());
        try {
            for (UserHistory<? extends Event> user: histories) {
                final long uid = user.getUserId();

                SparseVector summary = userSummarizer.summarize(user);
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
        } finally {
            histories.close();
        }

        return workMatrix;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected ItemItemModelBuildStrategy createBuildStrategy(ItemSimilarity similarity) {
        if (similarity.isSparse()) {
            return new SparseModelBuildStrategy(similarity);
        } else {
            return new SimpleModelBuildStrategy(similarity);
        }
    }
}
