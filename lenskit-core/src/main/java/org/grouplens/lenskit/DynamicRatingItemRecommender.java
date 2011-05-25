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

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Rating recommender recommending items from ratings-based user profiles.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface DynamicRatingItemRecommender {
    /**
     * Recommend all possible items for a user. The
     * exclude set is the set of keys in <var>ratings</var> (so items the user
     * has rated are not recommended).
     * @param user The user ID.
     * @param ratings The user's rating vector.
     * @return The sorted list of scored items.
     * @see #recommend(long, SparseVector, int, Set, Set)
     */
    public List<ScoredId> recommend(long user, SparseVector ratings);
    
    /**
     * Recommend up to <var>n</var> items for a user. The
     * exclude set is the set of keys in <var>ratings</var> (so items the user
     * has rated are not recommended).
     * @param user The user ID.
     * @param ratings The user's rating vector.
     * @param n The number of recommendations to return.
     * @return The sorted list of scored items.
     * @see #recommend(long, SparseVector, int, Set, Set)
     */
    public List<ScoredId> recommend(long user, SparseVector ratings, int n);

    /**
     * Recommend all possible items for a user from a set of candidates.  The
     * exclude set is the set of keys in <var>ratings</var> (so items the user
     * has rated are not recommended).
     * @param user The user ID.
     * @param ratings The user's rating vector.
     * @param candidates The candidate set (can be null to represent the
     * universe).
     * @return The sorted list of scored items.
     * @see #recommend(long, SparseVector, int, Set, Set)
     */
    public List<ScoredId> recommend(long user, SparseVector ratings,
            @Nullable Set<Long> candidates);

    /**
     * Produce a set of recommendations for the user.
     * @param user The user's ID
     * @param ratings The user's ratings
     * @param n The number of ratings to return.  If negative, recommend all
     * possible items.
     * @param candidates A set of candidate items which can be recommended.  If
     * <tt>null</tt>, the candidate set is considered to contain the universe.
     * @param exclude A set of items to be excluded.  If <tt>null</tt>, it is
     * considered the empty set.  Exclusions are applied to the candidate set,
     * so the final candidate set is <var>candidates</var> minus <var>exclude</var>.
     * @return a list of scored recommendations, sorted in nondecreasing order
     * of score.
     */
    public List<ScoredId> recommend(long user, SparseVector ratings, int n,
            @Nullable Set<Long> candidates, @Nullable Set<Long> exclude);
}
