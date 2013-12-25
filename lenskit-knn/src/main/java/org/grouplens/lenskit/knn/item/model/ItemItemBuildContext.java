/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.transform.normalize.VectorNormalizer;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.grapht.annotation.DefaultProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Iterator;

/**
 * Encapsulation of data needed during an item-item model build.  This class
 * provides access to item vectors and the item universe for use in  building
 * up the model in the accumulator.
 *
 * <p>This is shareable to make it more usable in the evaluator.  Typical built models
 * will not include it, and any dependencies on it should be {@link org.grouplens.lenskit.core.Transient}.</p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @see ItemItemModelBuilder
 */
@DefaultProvider(ItemItemBuildContextProvider.class)
@Shareable
public class ItemItemBuildContext implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull
    private
    LongKeyDomain items;
    @Nonnull
    private
    SparseVector[] itemVectors;

    @Nonnull
    private Long2ObjectMap<LongSortedSet> userItems;

    /**
     * Set up a new item build context.
     *
     * @param universe The set of items for the model.
     * @param vectors  Map of item IDs to item rating vectors.
     * @param userItems Map of user IDs to candidate items
     */
    ItemItemBuildContext(@Nonnull LongKeyDomain universe,
                         @Nonnull SparseVector[] vectors,
                         @Nonnull Long2ObjectMap<LongSortedSet> userItems) {
        this.userItems = userItems;
        items = universe;
        itemVectors = vectors;
    }

    /**
     * Get the set of items.
     *
     * @return The set of all items to build a model over.
     */
    @Nonnull
    public LongSortedSet getItems() {
        return items.activeSetView();
    }

    /**
     * Get the rating vector for an item. Rating vectors contain normalized ratings,
     * using the applicable {@link VectorNormalizer} on the user rating vectors.
     *
     * @param item The item to query.
     * @return The rating vector for {@code item}.
     * @throws IllegalArgumentException if {@code item} is not a valid item.
     */
    @Nonnull
    public SparseVector itemVector(long item) {
        int idx = items.getIndex(item);
        Preconditions.checkArgument(idx >= 0, "unknown item");
        return itemVectors[idx];
    }

    /**
     * Get the items rated by a particular user.
     * 
     * @param user The user to query for.
     * @return The items rated by {@code user}.
     */
    @Nonnull
    public LongSortedSet getUserItems(long user) {
        LongSortedSet items = userItems.get(user);
        if (items == null) {
            items = LongSortedSets.EMPTY_SET;
        }
        return items;
    }

    /**
     * Provides an Iterable over ItemVecPairs
     *
     * @return An Iterable over ItemVecPairs, objects
     *         pairing item ids and their corresponding vectors.
     */
    public Iterable<ItemVecPair> getItemPairs() {
        return new Iterable<ItemVecPair>() {
            @Override
            public Iterator<ItemVecPair> iterator() {
                return getItemPairIterator();
            }
        };
    }

    /**
     * Returns an Iterator over all item vector pairs.
     *
     * @return An Iterator over ItemVecPairs, an object
     *         offering public access to the item ids and their
     *         corresponding vectors.
     */
    public Iterator<ItemVecPair> getItemPairIterator() {
        return new FastIteratorImpl(items.activeSetView(), items.activeSetView());
    }

    /**
     * An Iterator implementation iterating over all ItemVecPairs from
     * the parameter LongSortedSets of item ids.
     */
    private final class FastIteratorImpl implements Iterator<ItemVecPair> {
        private ItemVecPair itemVecPair;
        private LongIterator iter1;
        private LongSortedSet list2;
        private LongIterator iter2;

        public FastIteratorImpl(LongSortedSet list1, LongSortedSet list2) {
            itemVecPair = new ItemVecPair();
            iter1 = list1.iterator();
            if (iter1.hasNext()) {
                itemVecPair.setItem1(iter1.nextLong());
            }
            this.list2 = list2;
            iter2 = list2.iterator();
        }

        @Override
        public boolean hasNext() {
            return iter1.hasNext() || iter2.hasNext();
        }

        @Override
        public ItemVecPair next() {
            if (!iter2.hasNext()) {
                itemVecPair.setItem1(iter1.nextLong());
                iter2 = list2.iterator();
            }
            itemVecPair.setItem2(iter2.nextLong());
            itemVecPair.lastInRow = !iter2.hasNext();
            return itemVecPair;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A pair of item ids and their corresponding item
     * vectors, avoiding (un)boxing the ids.
     */
    public final class ItemVecPair {
        public long itemId1;
        public long itemId2;
        public SparseVector vec1;
        public SparseVector vec2;
        public boolean lastInRow;

        public void setItem1(long itemId1) {
            this.itemId1 = itemId1;
            vec1 = itemVector(itemId1);
        }

        public void setItem2(long itemId2) {
            this.itemId2 = itemId2;
            vec2 = itemVector(itemId2);
        }
    }
}
