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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.data.history.ItemVector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Encapsulation of data needed during an item-item model build.  This class
 * provides access to item vectors, the item universe, and user-item sets to
 * be used by the build strategies to build up the model in the accumulator.
 *
 * @see ItemItemModelBuildStrategy
 * @see ItemItemModelProvider
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemBuildContext {
    private @Nonnull LongSortedSet items;
    private @Nonnull Long2ObjectMap<ItemVector> itemVectors;
    private @Nullable Long2ObjectMap<LongSortedSet> userItemSets;

    /**
     * Set up a new item build context.
     * @param universe The set of items for the model.
     * @param vectors Map of item IDs to item rating vectors.
     * @param userSets Optional map of users to rated item sets.
     */
    public ItemItemBuildContext(@Nonnull LongSortedSet universe,
                                @Nonnull Long2ObjectMap<ItemVector> vectors,
                                @Nullable Long2ObjectMap<LongSortedSet> userSets) {
        items = universe;
        itemVectors = vectors;
        userItemSets = userSets;
    }

    /**
     * Get the set of items.
     * @return The set of all items to build a model over.
     */
    @Nonnull
    public LongSortedSet getItems() {
        return items;
    }

    /**
     * Get the rating vector for an item. Rating vectors contain normalized ratings,
     * using the applicable {@link VectorNormalizer} on the user rating vectors.
     * @param item The item to query.
     * @return The rating vector for {@code item}.
     * @throws IllegalArgumentException if {@code item} is not a valid item.
     */
    @Nonnull
    public ItemVector itemVector(long item) {
        Preconditions.checkArgument(items.contains(item), "unknown item");
        assert itemVectors.containsKey(item);
        return itemVectors.get(item);
    }

    /**
     * Get the set of items rated by a user.
     * @param user The user to query.
     * @return The set of items rated by {@code user}.
     * @throws IllegalArgumentException if {@code user} is unknown.
     * @throws IllegalStateException if the build context did not collect user item sets.
     * @see ItemItemModelBuildStrategy#needsUserItemSets()
     */
    @Nonnull
    public LongSortedSet userItems(long user) {
        if (userItemSets == null) {
            throw new IllegalStateException("build context doesn't have user item sets");
        } else {
            LongSortedSet set = userItemSets.get(user);
            if (set == null) {
                throw new IllegalArgumentException("unknown user");
            } else {
                return set;
            }
        }
    }
}
