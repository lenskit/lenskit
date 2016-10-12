/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.api;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;


/**
 * Recommends items that go with a set of reference items. This interface is distinguished from
 * {@link ItemRecommender} in that it uses a set of reference items instead
 * of a user as the basis for computing scores.
 *
 * @see ItemRecommender
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @since 0.10
 */
public interface ItemBasedItemRecommender {
    /**
     * Recommend all possible items for a reference item using the default exclude set.
     *
     * @param reference The reference item.
     * @return The recommended items.
     * @see #recommendRelatedItems(Set, int, Set, Set)
     */
    List<Long> recommendRelatedItems(long reference);

    /**
     * Recommend up to <var>n</var> possible items for a reference item using the default exclude set.
     *
     * @param reference The reference item.
     * @param n The number of items to recommend (< 0 for unlimited).
     * @return The recommended items.
     * @see #recommendRelatedItems(Set, int, Set, Set)
     */
    List<Long> recommendRelatedItems(long reference, int n);

    /**
     * Recommend all possible items for a set of reference items using the default exclude set.
     *
     * @param basket The reference items.
     * @return The recommended items.
     * @see #recommendRelatedItems(Set, int, Set, Set)
     */
    List<Long> recommendRelatedItems(Set<Long> basket);

    /**
     * Recommend up to <var>n</var> items for a set of reference items using the default exclude set.
     *
     * @param basket The reference items.
     * @param n     The number of recommendations to return (< 0 for unlimited).
     * @return The recommended items.
     * @see #recommendRelatedItems(Set, int, Set, Set)
     */
    List<Long> recommendRelatedItems(Set<Long> basket, int n);

    /**
     * Produce a set of recommendations for the item. This method allows the recommendations to be constrained
     * by both a candidate set and an exclude set. The exclude set is applied to the candidate set, so the
     * final effective candidate set is <var>candidates</var> minus <var>exclude</var>.
     *
     * @param basket     The reference items.
     * @param n          The number of ratings to return. If negative, no specific size is requested.
     * @param candidates A set of candidate items which can be recommended. If {@code null}, all
     *                   items are considered candidates.
     * @param exclude    A set of items to be excluded. If {@code null}, a default exclude set is
     *                   used.
     * @return A list of recommended items.
     */
    List<Long> recommendRelatedItems(Set<Long> basket, int n, @Nullable Set<Long> candidates,
                                     @Nullable Set<Long> exclude);

    /**
     * Produce a set of recommendations for the item, with details. This is the most general recommendation
     * method, allowing the recommendations to be constrained by both a candidate set and an exclude
     * set and potentially providing more details on each recommendation. The exclude set is applied to the
     * candidate set, so the final effective candidate set is <var>candidates</var> minus <var>exclude</var>.
     *
     * @param basket     The reference items.
     * @param n          The number of ratings to return. If negative, no specific size is requested.
     * @param candidates A set of candidate items which can be recommended. If {@code null}, all
     *                   items are considered candidates.
     * @param exclude    A set of items to be excluded. If {@code null}, a default exclude set is
     *                   used.
     * @return A list of recommended items with recommendation details. If the recommender cannot assign
     *         meaningful scores, the scores will be {@link Double#NaN}.
     */
    ResultList recommendRelatedItemsWithDetails(Set<Long> basket, int n, @Nullable Set<Long> candidates,
                                                @Nullable Set<Long> exclude);
}
