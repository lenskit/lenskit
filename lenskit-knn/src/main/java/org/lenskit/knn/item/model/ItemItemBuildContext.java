/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.knn.item.model;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.longs.LongSortedSets;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.inject.Transient;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Encapsulation of data needed during an item-item model build.  This class
 * provides access to item vectors and the item universe for use in  building
 * up the model in the accumulator.
 *
 * <p>This is shareable to make it more usable in the evaluator.  Typical built models
 * will not include it, and any dependencies on it should be {@link Transient}.</p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @see ItemItemModelProvider
 */
@DefaultProvider(ItemItemBuildContextProvider.class)
@Shareable
@Immutable
public class ItemItemBuildContext implements Serializable {
    private static final long serialVersionUID = 2L;

    @Nonnull
    private
    SortedKeyIndex items;
    @Nonnull
    private
    Long2DoubleSortedMap[] itemVectors;

    @Nonnull
    private Long2ObjectMap<LongSortedSet> userItems;

    /**
     * Set up a new item build context.
     *  @param universe The set of items for the model.
     * @param vectors  Map of item IDs to item rating vectors.
     * @param userItems Map of user IDs to candidate items
     */
    ItemItemBuildContext(@Nonnull SortedKeyIndex universe,
                         @Nonnull Long2DoubleSortedMap[] vectors,
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
        return items.keySet();
    }

    /**
     * Get the rating vector for an item. Rating vectors contain normalized ratings,
     * using the applicable user-vector normalizer on the user rating vectors.
     *
     * @param item The item to query.
     * @return The rating vector for {@code item}.
     * @throws IllegalArgumentException if {@code item} is not a valid item.
     */
    @Nonnull
    public Long2DoubleSortedMap itemVector(long item) {
        int idx = items.tryGetIndex(item);
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
}
