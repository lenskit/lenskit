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
package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;

/**
 * Create a data context used to train slim model
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(SLIMBuildContextProvider.class)
public class SLIMBuildContext implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull
    private final
    Long2ObjectMap<Long2DoubleSortedMap> itemVectors;
    @Nonnull
    private final
    Long2ObjectMap<LongSortedSet> itemNeighbors;
    @Nonnull
    private final
    Long2ObjectMap<Long2DoubleSortedMap> innerProducts;
    @Nonnull
    private final Long2ObjectMap<LongSortedSet> userItems;


    /**
     * Set up a new item build context
     *
     * @param itemRatings Map of item IDs to item rating vectors.
     * @param itemNgbrs Map of item IDs to neighbor items ids.
     * @param innerProds Map of item IDs to inner-products with other item rating vectors
     * @param userItemSets of user IDs to user rated items
     */
    public SLIMBuildContext(@Nonnull Long2ObjectMap<Long2DoubleSortedMap> itemRatings,
                            @Nonnull Long2ObjectMap<LongSortedSet> itemNgbrs,
                            @Nonnull Long2ObjectMap<Long2DoubleSortedMap> innerProds,
                            @Nonnull Long2ObjectMap<LongSortedSet> userItemSets) {
        itemVectors = itemRatings;
        itemNeighbors = itemNgbrs;
        innerProducts = innerProds;
        userItems = userItemSets;
    }


    /**
     * Get all neighbors' inner-products with the given item id
     * @param itemId The item id to query for
     * @return a mapping of neighborhoods' id to inner-products
     */
    @Nonnull
    public Long2DoubleSortedMap getInnerProducts(long itemId) {
        Long2DoubleSortedMap innerProduct = innerProducts.get(itemId);
        if (innerProduct == null) {
            innerProduct = Long2DoubleSortedMaps.EMPTY_MAP;
        }
        return innerProduct;
    }

    @Nonnull
    public Long2ObjectMap<Long2DoubleSortedMap> getInnerProducts() { return innerProducts;}

    /**
     * Get the items rated by a particular user
     * @param user The user to query for
     * @return The items rated by {@code user}
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
     * Get the rating map for a given item {@code item}
     * @param item The item to query for
     * @return Map of item ids to ratings
     */
    @Nonnull
    public Long2DoubleSortedMap getItemRatings(long item) {
        Long2DoubleSortedMap itemRatings = itemVectors.get(item);
        if (itemRatings == null) {
            itemRatings = Long2DoubleSortedMaps.EMPTY_MAP;
        }
        return itemRatings;
    }

    /**
     * Get a rating matrix.
     *
     * @return Map of item IDs to item rating vectors.
     */
    @Nonnull
    public Long2ObjectMap<Long2DoubleSortedMap> getItemVectors() {
        return itemVectors;
    }

    /**
     * Get all item ids
     * @return Set of all item IDs.
     */
    @Nonnull
    public LongSortedSet getItemUniverse() {
        return LongUtils.frozenSet(itemVectors.keySet());
    }

    /**
     * Get all user ids
     * @return Set of all user IDs.
     */
    @Nonnull
    public LongSortedSet getAllUsers() {
        return LongUtils.frozenSet(userItems.keySet());
    }
    /**
     * Get neighborhood items for a given item {@code item}
     * @param item The item id to query for
     * @return Set of neighborhood item IDs.
     */
    @Nonnull
    public LongSortedSet getItemNeighbors(long item) {
        LongSortedSet ngbrs = itemNeighbors.get(item);
        if (ngbrs == null) {
            ngbrs = LongSortedSets.EMPTY_SET;
        }
        return ngbrs;
    }

}
