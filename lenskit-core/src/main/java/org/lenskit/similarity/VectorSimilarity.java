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
package org.lenskit.similarity;


import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;

/**
 * Compute the similarity between sparse vectors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(CosineVectorSimilarity.class)
public interface VectorSimilarity {

    /**
     * Compute the similarity between two vectors.
     *
     * @param vec1 The left vector to compare.
     * @param vec2 The right vector to compare.
     * @return The similarity, in the range [-1,1].
     */
    double similarity(Long2DoubleMap vec1, Long2DoubleMap vec2);

    /**
     * Query whether this similarity function is sparse (returns 0 for vectors with
     * disjoint key sets).
     *
     * @return {@code true} iff {@link #similarity(Long2DoubleMap, Long2DoubleMap)} will always return
     *         true when applied to two vectors with no keys in common.
     */
    boolean isSparse();

    /**
     * Query whether this similarity function is symmetric. Symmetric similarity functions
     * return the same result when called on (A,B) and (B,A).
     *
     * @return {@code true} if the function is symmetric.
     */
    boolean isSymmetric();
}
