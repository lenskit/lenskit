package org.grouplens.lenskit.data.dao;

import org.grouplens.lenskit.data.Rating;

/**
 * Listener for rating updates.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface RatingUpdateListener {
    /**
     * Called when a rating has been updated.
     * @param oldRating The old rating (or <tt>null</tt> if this is a new rating).
     * @param newRating The new rating (or <tt>null</tt> if this is a deleted rating).
     */
    void ratingUpdated(Rating oldRating, Rating newRating);
}
