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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.grapht.annotation.DefaultImplementation;

import javax.annotation.Nonnull;

/**
 * Item-item similarity model. It makes available the similarities
 * between items in the form of allowing queries to neighborhoods.
 * <p>
 * These similarities are post-normalization, so code using them
 * should typically use the same normalizations used by the builder
 * to make use of the similarity scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10 as an interface.
 */
@DefaultImplementation(SimilarityMatrixModel.class)
public interface ItemItemModel {
    /**
     * Get the set of all items in the model.
     *
     * @return The set of item IDs for all items in the model.
     */
    LongSortedSet getItemUniverse();

    /**
     * Get the neighbors of an item scored by similarity. This is the corresponding
     * <em>row</em> of the item-item similarity matrix (see {@link org.lenskit.knn.item}).
     *
     * @param item The item to get the neighborhood for.
     * @return The row of the similarity matrix. If the item is unknown, an empty
     *         vector is returned.
     */
    @Nonnull
    Long2DoubleMap getNeighbors(long item);
}
