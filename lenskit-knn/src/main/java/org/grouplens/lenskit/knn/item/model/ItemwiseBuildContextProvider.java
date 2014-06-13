/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.knn.item.model;

import com.google.common.collect.FluentIterable;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.ItemEventCollection;
import org.grouplens.lenskit.transform.normalize.ItemVectorNormalizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

/**
 * Builder for {@link ItemItemBuildContext} that normalizes per-item, not per-user.  More efficient
 * when using e.g. item-based normalization.  Right now it only works for rating data.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemwiseBuildContextProvider implements Provider<ItemItemBuildContext> {
    private static final Logger logger = LoggerFactory.getLogger(ItemwiseBuildContextProvider.class);

    private final ItemEventDAO itemEventDAO;
    private final ItemDAO itemDAO;
    private final ItemVectorNormalizer normalizer;

    /**
     * Construct a new build context provider.
     * @param edao The item-event DAO.
     * @param idao The item DAO.
     * @param norm The item vector normalizer.  This is applied to item rating vectors.  You should
     *             take care to use a compatible normalizer for the item scorer (e.g. if this uses
     *             a {@link org.grouplens.lenskit.transform.normalize.MeanCenteringVectorNormalizer},
     *             then you should use {@link org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer}
     *             for the user vector normalization in the scorer).
     */
    @Inject
    public ItemwiseBuildContextProvider(@Transient ItemEventDAO edao, @Transient ItemDAO idao,
                                        @Transient ItemVectorNormalizer norm) {
        itemEventDAO = edao;
        itemDAO = idao;
        normalizer = norm;
    }

    /**
     * Constructs and returns a new ItemItemBuildContext.
     *
     * @return a new ItemItemBuildContext.
     */
    @Override
    public ItemItemBuildContext get() {
        logger.info("constructing build context");
        logger.debug("using normalizer {}", normalizer);

        logger.debug("Building item data");
        Long2ObjectMap<LongList> userItems = new Long2ObjectOpenHashMap<LongList>(1000);
        Long2ObjectMap<SparseVector> itemVectors = new Long2ObjectOpenHashMap<SparseVector>(1000);
        Cursor<ItemEventCollection<Event>> itemCursor = itemEventDAO.streamEventsByItem();
        try {
            for (ItemEventCollection<Event> item: itemCursor.fast()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("processing {} ratings for item {}", item.size(), item);
                }
                List<Rating> ratings = FluentIterable.from(item)
                                                     .filter(Rating.class)
                                                     .toList();
                MutableSparseVector vector = Ratings.itemRatingVector(ratings);
                normalizer.normalize(item.getItemId(), vector, vector);
                for (VectorEntry e: vector.fast()) {
                    long user = e.getKey();
                    LongList uis = userItems.get(user);
                    if (uis == null) {
                        // lists are nice and fast, we only see each item once
                        uis = new LongArrayList();
                        userItems.put(user, uis);
                    }
                    uis.add(item.getItemId());
                }
                itemVectors.put(item.getItemId(), vector.freeze());
            }
        } finally {
            itemCursor.close();
        }

        Long2ObjectMap<LongSortedSet> userItemSets = new Long2ObjectOpenHashMap<LongSortedSet>();
        for (Long2ObjectMap.Entry<LongList> entry: CollectionUtils.fast(userItems.long2ObjectEntrySet())) {
            userItemSets.put(entry.getLongKey(), LongUtils.packedSet(entry.getValue()));
        }

        LongKeyDomain items = LongKeyDomain.fromCollection(itemVectors.keySet(), true);
        SparseVector[] itemData = new SparseVector[items.domainSize()];
        for (int i = 0; i < itemData.length; i++) {
            long itemId = items.getKey(i);
            itemData[i] = itemVectors.get(itemId);
        }

        logger.debug("item data completed");
        return new ItemItemBuildContext(items, itemData, userItemSets);
    }
}
