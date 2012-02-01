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

import java.util.Collection;

import javax.annotation.Nonnull;

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
     * Score a single item.
     *
     * @param queryItem The objective item ID used as the query
     * @param item The item ID to score.
     * @return The preference, or {@link Double#NaN} if no preference can be
     *         predicted.
     */
	double globalScore(long queryItem, long item);

    /**
     * Score a collection of items.
     *
     * @param queryItem The objective item ID used as the query
     * @param items The list of items to score.
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
	@Nonnull
	SparseVector globalScore(long queryItem, Collection<Long> items);



}
