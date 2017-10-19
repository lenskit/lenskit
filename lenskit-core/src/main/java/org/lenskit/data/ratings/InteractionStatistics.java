/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.data.ratings;

import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.inject.Shareable;
import org.lenskit.inject.Transient;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.SortedKeyIndex;

import net.jcip.annotations.Immutable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;

/**
 * Interaction statistics, counting the number of times an item has been interacted with.
 * The interaction entity must have {@link CommonAttributes#ITEM_ID} attributes.
 */
@Shareable
@Immutable
@DefaultProvider(InteractionStatistics.ISProvider.class)
public class InteractionStatistics implements Serializable {
    private static final long serialVersionUID = 1L;

    private final EntityType entityType;
    private final SortedKeyIndex items;
    private final int[] interactionCounts;
    private final LongArrayList itemList;

    /**
     * Construct a new interaction statistics object.
     *
     * @param type The counted entity type.
     * @param counts A map of item interaction counts.
     */
    public InteractionStatistics(EntityType type, Long2IntMap counts) {
        entityType = type;
        items = SortedKeyIndex.fromCollection(counts.keySet());
        int n = items.size();
        interactionCounts = new int[n];
        for (int i = 0; i < n; i++) {
            interactionCounts[i] = counts.get(items.getKey(i));
        }
        long[] iarray = items.keySet().toLongArray();
        LongArrays.quickSort(iarray, (l1, l2) -> Doubles.compare(counts.get(l2), counts.get(l1)));
        itemList = LongArrayList.wrap(iarray);
    }

    /**
     * Construct a new interaction statistics object.
     * @param dao The DAO.
     * @param type The entity type to count.
     * @return The statistics.
     */
    public static InteractionStatistics create(DataAccessObject dao, EntityType type) {
        return new ISProvider(type, dao).get();
    }

    /**
     * Construct a new interaction statistics object that counts ratings.
     * @param dao The DAO.
     * @return The rating statistics.
     */
    public static InteractionStatistics create(DataAccessObject dao) {
        return create(dao, CommonTypes.RATING);
    }

    /**
     * Get the interaction entity type.
     * @return The type of entities counted for this statistics object.
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Get the number of interactions for an item.
     * @param item The item.
     * @return The number of interactions for `item`.
     */
    public int getInteractionCount(long item) {
        int idx = items.tryGetIndex(item);
        if (idx >= 0) {
            return interactionCounts[idx];
        } else {
            return 0;
        }
    }

    /**
     * Get the set of known items.
     * @return The set of known items.
     */
    public LongSortedSet getKnownItems() {
        return items.keySet();
    }

    /**
     * Get the list of items by decreasing popularity.
     * @return The list of items, ordered by non-increasing popularity.
     */
    public LongList getItemsByPopularity() {
        return LongLists.unmodifiable(itemList);
    }

    /**
     * Provider that counts item interactions.
     */
    public static class ISProvider implements Provider<InteractionStatistics> {
        private final EntityType entityType;
        private final DataAccessObject dao;

        /**
         * Construct the provider.
         * @param type The entity type. It should have {@link CommonAttributes#ITEM_ID} attributes.
         * @param dao The data access object.
         */
        @Inject
        public ISProvider(@InteractionEntityType EntityType type,
                          @Transient DataAccessObject dao) {
            entityType = type;
            this.dao = dao;
        }

        @Override
        public InteractionStatistics get() {
            Long2IntOpenHashMap counts = new Long2IntOpenHashMap();

            try (ObjectStream<Entity> stream = dao.query(entityType).stream()) {
                for (Entity e : stream) {
                    long item = e.getLong(CommonAttributes.ITEM_ID);
                    counts.addTo(item, 1);
                }
            }

            return new InteractionStatistics(entityType, counts);
        }
    }

    /**
     * Provider that counts item interactions.
     */
    public static class CountSumISProvider implements Provider<InteractionStatistics> {
        private final EntityType entityType;
        private final DataAccessObject dao;

        /**
         * Construct the provider.
         * @param type The entity type. It should have {@link CommonAttributes#ITEM_ID} attributes.
         * @param dao The data access object.
         */
        @Inject
        public CountSumISProvider(@InteractionEntityType EntityType type,
                                  @Transient DataAccessObject dao) {
            entityType = type;
            this.dao = dao;
        }

        @Override
        public InteractionStatistics get() {
            Long2IntOpenHashMap counts = new Long2IntOpenHashMap();

            try (ObjectStream<Entity> stream = dao.query(entityType).stream()) {
                for (Entity e : stream) {
                    long item = e.getLong(CommonAttributes.ITEM_ID);
                    counts.addTo(item, e.getInteger(CommonAttributes.COUNT));
                }
            }

            return new InteractionStatistics(entityType, counts);
        }
    }
}
