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
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.event.Event;

/**
 * Rating recommender recommending items from rofiles. This class is like
 * {@link ItemRecommender}, but it takes a user's history (or a subset thereof)
 * as input.
 * 
 * @see ItemRecommender
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface DynamicItemRecommender {
    /**
     * Recommend all possible items for a user with the default exclude set.
     * 
     * @param profile The user profile.
     * @return The sorted list of scored items.
     * @see #recommend(UserHistory, int, Set, Set)
     */
    public ScoredLongList recommend(UserHistory<? extends Event> profile);
    
    /**
     * Recommend up to <var>n</var> items for a user using the default exclude
     * set.
     * 
     * @param profile The user profile.
     * @param n The number of recommendations to return.
     * @return The sorted list of scored items.
     * @see #recommend(UserHistory, int, Set, Set)
     */
    public ScoredLongList recommend(UserHistory<? extends Event> profile, int n);

    /**
     * Recommend all possible items for a user from a set of candidates using
     * the default exclude set.
     * 
     * @param profile The user profile.
     * @param candidates The candidate set (can be null to represent the
     *        universe).
     * @return The sorted list of scored items.
     * @see #recommend(UserHistory, int, Set, Set)
     */
    public ScoredLongList recommend(UserHistory<? extends Event> profile,
            @Nullable Set<Long> candidates);

    /**
     * Produce a set of recommendations for the user.
     * 
     * @param profile The user profile.
     * @param n The number of ratings to return. If negative, recommend all
     *        possible items.
     * @param candidates A set of candidate items which can be recommended. If
     *        <tt>null</tt>, the candidate set is considered to contain the
     *        universe.
     * @param exclude A set of items to be excluded. If <tt>null</tt>, the
     *        default exclude set is used. Exclusions are applied to the
     *        candidate set, so the final candidate set is <var>candidates</var>
     *        minus <var>exclude</var>.
     * @return a list of scored recommendations, sorted in nondecreasing order
     *         of score.
     * @see ItemRecommender#recommend(long, int, Set, Set)
     */
    public ScoredLongList recommend(UserHistory<? extends Event> profile, int n,
            @Nullable Set<Long> candidates, @Nullable Set<Long> exclude);
}
