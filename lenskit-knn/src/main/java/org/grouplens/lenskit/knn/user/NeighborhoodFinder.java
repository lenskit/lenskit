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
package org.grouplens.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.params.meta.DefaultClass;

/**
 * Interface for neighborhood-finding strategies. These strategies are used by
 * {@link UserUserRecommender} to find neighbors for recommendation.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@DefaultClass(SimpleNeighborhoodFinder.class)
public interface NeighborhoodFinder {
    /**
     * Find neighboring users for particular items. <var>user</var> and the
     * returned rating vectors are <emph>unnormalized</emph>.  Any normalization
     * used by the neighborhood finder is only for comparing neighbors.
     * @param user The user's event history.
     * @param items The items we're trying to recommend, or <tt>null</tt> to get
     * get neighborhoods for all possible items.
     * @return A map from item IDs to user neighborhoods for all items for which
     * we can find neighboring users.
     */
    Long2ObjectMap<? extends Collection<Neighbor>> findNeighbors(
            @Nonnull UserHistory<? extends Event> user, @Nullable LongSet items);
}
