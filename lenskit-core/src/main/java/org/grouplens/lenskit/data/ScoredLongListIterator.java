package org.grouplens.lenskit.data;

import it.unimi.dsi.fastutil.longs.LongListIterator;

/**
 * Iterator for {@link ScoredLongList}s.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface ScoredLongListIterator extends LongListIterator {
    /**
     * Get the score of the last item returned by a call to {@link #previous()}
     * or {@link #next()}.
     * @return The item's score.
     */
    double getScore();

    /**
     * Set the score of the last item returned by a call to {@link #previous()}
     * or {@link #next()} (optional operation).
     * 
     * @param s The new score.
     * @throws UnsupportedOperationException if the set/setScore operation is
     *             not supported.
     * @see #set(Long)
     */
    void setScore(double s);
}
