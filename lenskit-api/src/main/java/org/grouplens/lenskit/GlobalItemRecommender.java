/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit;

import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.lenskit.collections.ScoredLongList;


/**
 * The interface for recommendation based on the items only. The difference from the {@link ItemRecommender} is
 * that the input is only the item or list of items instead of user specific information. This is a Find Similar
 * Items/ People Also Liked recommendation.
 *
 * <p>
 * The implementation is analogous to the {@link ItemRecommender}.
 * </p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @since 0.10
 */
public interface GlobalItemRecommender {


    /**
     * Recommend all possible items for a basket of items using the default exclude set.
     *
     * @param items The items value.
     * @return The sorted list of scored items.
     * @see #globalRecommend(Set, int, Set, Set)
     */
    ScoredLongList globalRecommend(Set<Long> items);

    /**
     * Recommend up to {@var n} items for a basket of items using the default exclude
     * set.
     *
     * @param items The items value.
     * @param n     The number of recommendations to return.
     * @return The sorted list of scored items.
     * @see #globalRecommend(Set, int, Set, Set)
     */
    ScoredLongList globalRecommend(Set<Long> items, int n);

    /**
     * Recommend all possible items for a basket of items from a set of candidates using
     * the default exclude set.
     *
     * @param items      The items value.
     * @param candidates The candidate set (can be null to represent the
     *                   universe).
     * @return The sorted list of scored items.
     * @see #globalRecommend(Set, int, Set, Set)
     */
    ScoredLongList globalRecommend(Set<Long> items, @Nullable Set<Long> candidates);

    /**
     * Produce a set of recommendations for the item. This is the most general
     * recommendation method, allowing the recommendations to be constrained by
     * both a candidate set and an exclude set. The exclude set is applied to
     * the candidate set, so the final effective candidate set is
     * {@var canditates} minus {@var exclude}.
     *
     * @param items      The items value
     * @param n          The number of ratings to return. If negative, recommend all
     *                   possible items.
     * @param candidates A set of candidate items which can be recommended. If
     *                   {@code null}, all items are considered candidates.
     * @param exclude    A set of items to be excluded. If {@code null}, a default
     *                   exclude set is used.
     * @return A list of recommended items. If the recommender cannot assign
     *         meaningful scores, the scores will be {@link Double#NaN}. For
     *         most scoring recommenders, the items should be ordered in
     *         decreasing order of score. This is not a hard requirement ??? e.g.
     *         set recommenders are allowed to be more flexible.
     */
    ScoredLongList globalRecommend(Set<Long> items, int n, @Nullable Set<Long> candidates,
                                   @Nullable Set<Long> exclude);
}
