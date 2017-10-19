/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
