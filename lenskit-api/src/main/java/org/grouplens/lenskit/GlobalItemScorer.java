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
package org.grouplens.lenskit;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;


/**
 * Score items for given item(s). These scores can be predicted relevance
 * scores, purchase probabilities, or any other real-valued score which can be
 * assigned to an item for a given item.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @since 0.10
 */
public interface GlobalItemScorer {
    /**
     * Score a single item based on a collection of items(a shopping basket).
     *
     * @param queryItems The objective items ID used as the query
     * @param item       The item ID to score.
     * @return The preference, or {@link Double#NaN} if no preference can be
     *         predicted.
     */
    double globalScore(@Nonnull Collection<Long> queryItems, long item);

    /**
     * Score a collection of items based on a collection of items(a shopping basket).
     *
     * @param queryItems The objective items ID used as the query
     * @param items      The list of items to score.
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
    @Nonnull
    SparseVector globalScore(@Nonnull Collection<Long> queryItems,
                             @Nonnull Collection<Long> items);

    /**
     * Score items in a vector based on a collection of items (a shopping basket).
     *
     * @param queryItems The items to use as the query.
     * @param output     A vector whose key domain is the items to score.
     * @see ItemScorer#score(long, MutableSparseVector)
     */
    void globalScore(@Nonnull Collection<Long> queryItems,
                     @Nonnull MutableSparseVector output);
}
