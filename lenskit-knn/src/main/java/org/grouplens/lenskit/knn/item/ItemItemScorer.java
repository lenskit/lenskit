package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.event.Event;

/**
 * Item scorer specific to item-item recommenders. It exposes the item-item
 * model as well as the scoring functionality.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface ItemItemScorer extends ItemScorer {
    /**
     * Get the item-item model backing this scorer.
     * 
     * @return The model this scorer uses to compute scores.
     */
    ItemItemModel getModel();
    
    /**
     * Get the set of scoreable items for a user.
     * @param user The user to query for.
     * @return The set of items for which scores can be generated.
     */
    LongSet getScoreableItems(UserHistory<? extends Event> user);
}
