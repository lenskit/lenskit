package org.grouplens.lenskit;

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.data.vector.SparseVector;

/** 
 * Rating predictor that supports discovering the predictable items for a
 * user.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface DiscoverableRatingPredictor extends RatingPredictor {
    /**
     * Get the set of predictable items for a user.
     * @param user The user ID
     * @param ratings The user's rating vector
     * @return The set of items for which predictions can be made for this user.
     */
    LongSet getPredictableItems(long user, SparseVector ratings);
}
