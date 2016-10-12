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

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;


/**
 * Score items with reference to specified item(s). These scores can be predicted relevance
 * scores, purchase probabilities, or any other real-valued score which can be
 * assigned to an item relative to a given item.  This interface is distinguished from
 * {@link ItemScorer} in that it uses a set of reference items instead
 * of a user as the basis for computing scores.
 *
 * @compat Public
 */
public interface ItemBasedItemScorer {
    /**
     * Score a single item based on a collection of items (e.g. a shopping basket).
     *
     * @param basket The objective items ID used as the query
     * @param item   The item ID to score.
     * @return The relevance score, or {@code null} if no score can be
     * predicted.
     */
    Result scoreRelatedItem(@Nonnull Collection<Long> basket, long item);

    /**
     * Score a collection of items based on a collection of items (e.g. a shopping basket).
     *
     * @param basket The objective items ID used as the query
     * @param items  The list of items to score.
     * @return A mapping from item IDs to relevance scores. This mapping may
     * not contain all requested items.
     */
    @Nonnull
    Map<Long, Double> scoreRelatedItems(@Nonnull Collection<Long> basket,
                                        @Nonnull Collection<Long> items);

    /**
     * Score a collection of items based on a collection of items (e.g. a shopping basket), with details.
     *
     * @param basket The items to use as the query.
     * @param items  The items to score.
     * @return The scores for the items, possibly with additional details (will be represented as a
     * subclass of {@link Result}.
     */
    ResultMap scoreRelatedItemsWithDetails(@Nonnull Collection<Long> basket, Collection<Long> items);
}
