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
