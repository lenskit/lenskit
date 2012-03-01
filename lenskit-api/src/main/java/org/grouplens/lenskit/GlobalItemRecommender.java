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

import org.grouplens.lenskit.collections.ScoredLongList;



/**
 * The interface for recommendation based on the items only. The difference from the {@link ItemRecommender} is 
 * that the input is only the item or list of items instead of user specific information. This is a Find Similar 
 * Items/ People Also Liked recommendation.
 * 
 * The implementation is analogous to the {@link ItemRecommender}.
 * 
 * @author Steven Chang <schang@cs.umn.edu>
 * 
 *
 */
public interface GlobalItemRecommender {
    /**
     * Recommend all possible items for an item using the default exclude set.
     *
     * @param item The item value.
     * @return The sorted list of scored items.
     * @see #globalRecommend(long, int, Set, Set)
     */
    ScoredLongList globalRecommend(long item);

    /**
     * Recommend up to <var>n</var> items for an item using the default exclude
     * set.
     *
     * @param item The item value.
     * @param n The number of recommendations to return.
     * @return The sorted list of scored items.
     * @see #globalRecommend(long, int, Set, Set)
     */
    ScoredLongList globalRecommend(long item, int n);

    /**
     * Recommend all possible items for an item from a set of candidates using
     * the default exclude set.
     *
     * @param item The item value.
     * @param candidates The candidate set (can be null to represent the
     *        universe).
     * @return The sorted list of scored items.
     * @see #globalRecommend(long, int, Set, Set)
     */
    ScoredLongList globalRecommend(long item, @Nullable Set<Long> candidates);

    /**
     * Produce a set of recommendations for the item. This is the most general
     * recommendation method, allowing the recommendations to be constrained by
     * both a candidate set and an exclude set. The exclude set is applied to
     * the candidate set, so the final effective candidate set is
     * <var>canditates</var> minus <var>exclude</var>.
     *
     * @param item The item value
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
    ScoredLongList globalRecommend(long item, int n, @Nullable Set<Long> candidates,
                             @Nullable Set<Long> exclude);
    
    /**
     * Recommend all possible items for a basket of items using the default exclude set.
     *
     * @param items The items value.
     * @return The sorted list of scored items.
     * @see #globalRecommend(long, int, Set, Set)
     */
    ScoredLongList globalRecommend(Set<Long> items);

    /**
     * Recommend up to <var>n</var> items for a basket of items using the default exclude
     * set.
     *
     * @param items The items value.
     * @param n The number of recommendations to return.
     * @return The sorted list of scored items.
     * @see #globalRecommend(long, int, Set, Set)
     */
    ScoredLongList globalRecommend(Set<Long> items, int n); 

    /**
     * Recommend all possible items for a basket of items from a set of candidates using
     * the default exclude set.
     *
     * @param items The items value.
     * @param candidates The candidate set (can be null to represent the
     *        universe).
     * @return The sorted list of scored items.
     * @see #globalRecommend(long, int, Set, Set)
     */
    ScoredLongList globalRecommend(Set<Long> items, @Nullable Set<Long> candidates);
    
    /**
     * See {@link #globalRecommend(long, int, Set, Set)}
     *
     * @param items The items value
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
    ScoredLongList globalRecommend(Set<Long> items, int n, @Nullable Set<Long> candidates,
                             @Nullable Set<Long> exclude);
}
