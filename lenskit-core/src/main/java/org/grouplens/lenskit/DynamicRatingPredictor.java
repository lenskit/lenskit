package org.grouplens.lenskit;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Interface for predicting ratings from user rating histories.  This interface
 * is like {@link RatingPredictor}, except the client passes in the user's
 * ratings to be used in computation.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface DynamicRatingPredictor {
    /**
     * Generate predictions for a collection of items.
     * @param user the user ID
     * @param ratings The user's ratings
     * @param items the items for which predictions are desired
     * @return A mapping from item IDs to predicted preference.  This mapping
     * may not contain all requested items.
     */
    @Nonnull
    public SparseVector predict(long user, Collection<Rating> ratings, Collection<Long> items);
    
    /**
     * Generate predictions for a collection of items.
     * @param user the user ID
     * @param ratings The user's rating vector
     * @param items the items for which predictions are desired
     * @return A mapping from item IDs to predicted preference.  This mapping
     * may not contain all requested items.
     */
    @Nonnull
    public SparseVector predict(long user, SparseVector ratings, Collection<Long> items);
}
