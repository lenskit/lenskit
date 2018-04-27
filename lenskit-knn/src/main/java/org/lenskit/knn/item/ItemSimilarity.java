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
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;

/**
 * Compute the similarity between two items.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
@DefaultImplementation(ItemVectorSimilarity.class)
public interface ItemSimilarity {
    /**
     * Compute the similarity between two items.
     *
     * @param i1 The first item ID.
     * @param v1 The first item vector.
     * @param i2 The second item ID.
     * @param v2 The second item vector.
     * @return The similarity between the two items, in the range [0,1].
     */
    double similarity(long i1, Long2DoubleMap v1, long i2, Long2DoubleMap v2);

    /**
     * Query whether this similarity is sparse.
     *
     * @return {@code true} if the similarity function is sparse.
     * @see org.lenskit.similarity.VectorSimilarity#isSparse()
     */
    boolean isSparse();

    /**
     * Query whether this similarity is symmetric.
     *
     * @return {@code true} if the similarity function is symmetric.
     * @see org.lenskit.similarity.VectorSimilarity#isSymmetric()
     */
    boolean isSymmetric();
}
