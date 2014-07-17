/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;

/**
 * Finds candidate neighbors for a user.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
@DefaultImplementation(LiveNeighborFinder.class)
public interface NeighborFinder {
    /**
     * Get potential neighbors for a particular user.
     *
     * @param user  The user whose neighbors are wanted.
     * @param items The items that the client needs to be able to score or recommend.
     * @return A collection of potential neighbors for {@code user}.  This collection may include
     *         neighbors that are not useful for scoring any item in {@code items}; the item set
     *         is just to help the neighbor finder guide its search if relevant.
     */
    Iterable<Neighbor> getCandidateNeighbors(UserHistory<? extends Event> user, LongSet items);
}
