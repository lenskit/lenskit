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
import net.jcip.annotations.ThreadSafe;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;

import javax.inject.Inject;
import java.io.Serializable;

import static java.lang.Math.max;

/**
 * Apply significance weighting to a similarity function. The threshold
 * is configured with the {@link SigWeightThreshold} parameter.
 *
 * <p>Significance weighting decreases the similarity between two vectors when
 * the number of common entities between the two vectors is low.  For a threshold
 * \(S\) and key sets \(K_1\) and \(K_2\), the similarity is multipled by
 * \[\frac{|K_1 \cap K_2|}{\mathrm{max}(|K_1 \cap K_2|, S)}\]
 *
 * <ul>
 * <li>Herlocker, J., Konstan, J.A., and Riedl, J. <a
 * href="http://dx.doi.org/10.1023/A:1020443909834">An Empirical Analysis of
 * Design Choices in Neighborhood-Based Collaborative Filtering Algorithms</a>.
 * <i>Information Retrieval</i> Vol. 5 Issue 4 (October 2002), pp. 287-310.</li>
 * </ul>
 *
 * @see SigWeightThreshold
 */
@Shareable
@ThreadSafe
public class SignificanceWeightedVectorSimilarity implements VectorSimilarity, Serializable {

    private static final long serialVersionUID = 1L;

    private final int threshold;
    private final VectorSimilarity delegate;

    @Inject
    public SignificanceWeightedVectorSimilarity(@SigWeightThreshold int thresh,
                                                VectorSimilarity sim) {
        threshold = thresh;
        delegate = sim;
    }

    /**
     * Get the underlying similarity (for debuggin purposes).
     * @return The wrapped vector similarity.
     */
    public VectorSimilarity getDelegate() {
        return delegate;
    }

    @Override
    public double similarity(Long2DoubleMap vec1, Long2DoubleMap vec2) {
        double s = delegate.similarity(vec1, vec2);
        int n = LongUtils.intersectSize(vec1.keySet(), vec2.keySet());
        s *= n;
        return s / max(n, threshold);
    }

    @Override
    public boolean isSparse() {
        return delegate.isSparse();
    }

    @Override
    public boolean isSymmetric() {
        return delegate.isSymmetric();
    }

    @Override
    public String toString() {
        return String.format("SigWeight(%s, %d)", delegate, threshold);
    }
}
