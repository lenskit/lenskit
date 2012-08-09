/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
 *
 * @since 1.0
 *
 * @author Steven Chang <schang@cs.umn.edu>
 *
 */
public interface GlobalItemScorer {
    /**
     * Score a single item based on a collection of items(a shopping basket).
     *
     * @param queryItems The objective items ID used as the query
     * @param item The item ID to score.
     * @return The preference, or {@link Double#NaN} if no preference can be
     *         predicted.
     */
	double globalScore(Collection<Long> queryItems, long item);

    /**
     * Score a collection of items based on a collection of items(a shopping basket).
     *
     * @param queryItems The objective items ID used as the query
     * @param items The list of items to score.
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
	@Nonnull
	SparseVector globalScore(Collection<Long> queryItems, Collection<Long> items);

    /**
     * Score a collection of items based on a collection of items (a shopping basket).
     * @param queryItems The items to use as the query.
     * @param output A vector whose key domain is the items to score.
     */
    void globalScore(Collection<Long> queryItems, MutableSparseVector output);
}
