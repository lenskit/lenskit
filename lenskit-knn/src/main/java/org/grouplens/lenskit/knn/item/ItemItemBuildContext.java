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
