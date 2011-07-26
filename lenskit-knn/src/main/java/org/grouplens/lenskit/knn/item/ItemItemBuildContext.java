/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Encapsulation of data needed during an item-item model build.  This class
 * provides access to item vectors, the item universe, and user-item sets to
 * be used by the build strategies to build up the model in the accumulator.
 * 
 * @see ItemItemModelBuildStrategy
 * @see ItemItemModelBuilder
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemBuildContext {
    private LongSortedSet items;
    private Long2ObjectMap<SparseVector> itemVectors;
    private Long2ObjectMap<LongSortedSet> userItemSets;
    
    public ItemItemBuildContext(LongSortedSet universe,
                                Long2ObjectMap<SparseVector> vectors,
                                Long2ObjectMap<LongSortedSet> userSets) {
        items = universe;
        itemVectors = vectors;
        userItemSets = userSets;
    }
    
    public LongSortedSet getItems() {
        return items;
    }
    
    public SparseVector itemVector(long item) {
        return itemVectors.get(item);
    }
    
    public LongSortedSet userItems(long user) {
        return userItemSets.get(user);
    }
}
