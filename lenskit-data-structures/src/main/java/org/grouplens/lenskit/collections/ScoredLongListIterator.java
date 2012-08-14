/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.longs.LongListIterator;

/**
 * Iterator for {@link ScoredLongList}s.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @compat Public
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
