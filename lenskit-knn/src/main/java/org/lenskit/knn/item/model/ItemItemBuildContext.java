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
package org.lenskit.knn.item.model;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.longs.LongSortedSets;
import net.jcip.annotations.Immutable;
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
