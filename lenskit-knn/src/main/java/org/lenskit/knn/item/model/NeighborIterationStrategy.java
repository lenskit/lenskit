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
package org.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.grapht.annotation.DefaultProvider;

/**
 * Abstraction of strategies for iterating over potential neighboring items.  This is used by the
 * item-item model builder to iterate over the potential neighbors of an item.  It is abstracted
 * so that different strategies can be used depending on the properties of the similarity function
 * and data set.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(DefaultNeighborIterationStrategyProvider.class)
public interface NeighborIterationStrategy {
    /**
     * Get an iterator over possible neighbors of an item.
     * @param context The build context (to get item &amp; neighbor information).
     * @param item The item ID.  The item may or may not be included in the returned items.
     * @param onlyAfter If {@code true}, only consider item IDs after {@code item}, because
     *                  the caller only needs unique unordered pairs.
     * @return An iterator over possible neighbors of {@code item}.
     */
    LongIterator neighborIterator(ItemItemBuildContext context, long item, boolean onlyAfter);
}
