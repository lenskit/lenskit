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
package org.grouplens.lenskit;

import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.ScoredLongList;

/**
 * Interface for recommending items.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface ItemRecommender {
    /**
     * Recommend all possible items for a user using the default exclude set.
     * 
     * @param user The user ID.
     * @return The sorted list of scored items.
     * @see #recommend(long, int, Set, Set)
     */
    ScoredLongList recommend(long user);

    /**
     * Recommend up to <var>n</var> items for a user using the default exclude
     * set.
     * 
     * @param user The user ID.
     * @param n The number of recommendations to return.
     * @return The sorted list of scored items.
     * @see #recommend(long, int, Set, Set)
     */
    ScoredLongList recommend(long user, int n);

    /**
     * Recommend all possible items for a user from a set of candidates using
     * the default exclude set.
     * 
     * @param user The user ID.
     * @param candidates The candidate set (can be null to represent the
     *        universe).
     * @return The sorted list of scored items.
     * @see #recommend(long, int, Set, Set)
     */
    ScoredLongList recommend(long user, @Nullable Set<Long> candidates);

    /**
     * Produce a set of recommendations for the user. This is the most general
     * recommendation method, allowing the recommendations to be constrained by
     * both a candidate set and an exclude set. The exclude set is applied to
     * the candidate set, so the final effective candidate set is
     * <var>canditates</var> minus <var>exclude</var>.
     * 
     * <p>
     * If the exclude set is <tt>null</tt>, a default exclude set is used. The
     * exact definition of this can vary between implementations, but will be a
     * sensible set designed to exclude items the user likely already has (e.g.
     * recommenders operating on user ratings will generally exclude items the
     * user has rated, and likewise purchase-based recommenders will typically
     * exclude items the user has purchased).
     * 
     * @param user The user's ID
     * @param n The number of ratings to return. If negative, recommend all
     *        possible items.
     * @param candidates A set of candidate items which can be recommended. If
     *        <tt>null</tt>, all items are considered candidates.
     * @param exclude A set of items to be excluded. If <tt>null</tt>, a default
     *        exclude set is used.
     * @return A list of recommended items. If the recommender cannot assign
     *         meaningful scores, the scores will be {@link Double#NaN}. For
     *         most scoring recommenders, the items should be ordered in
     *         decreasing order of score. This is not a hard requirement — e.g.
     *         set recommenders are allowed to be more flexible.
     */
    ScoredLongList recommend(long user, int n, @Nullable Set<Long> candidates,
                             @Nullable Set<Long> exclude);
}
