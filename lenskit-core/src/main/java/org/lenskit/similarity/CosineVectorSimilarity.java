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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import net.jcip.annotations.ThreadSafe;
import org.lenskit.inject.Shareable;
import org.lenskit.util.math.Scalars;
import org.lenskit.util.math.Vectors;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Cosine similarity for vectors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@ThreadSafe
public class CosineVectorSimilarity implements VectorSimilarity, Serializable {
    private static final long serialVersionUID = 1L;

    private final double dampingFactor;

    /**
     * Construct an undamped cosine similarity function.
     */
    public CosineVectorSimilarity() {
        this(0.0);
    }

    /**
     * Construct a new cosine similarity function.
     *
     * @param damping The Bayesian damping term (added to denominator), to bias the
     *                similarity towards 0 for low-cooccurance vectors.
     */
    @Inject
    public CosineVectorSimilarity(@SimilarityDamping double damping) {
        Preconditions.checkArgument(damping >= 0, "negative damping not allowed");
        dampingFactor = damping;
    }

    @Override
    public double similarity(Long2DoubleMap vec1, Long2DoubleMap vec2) {
        final double dot = Vectors.dotProduct(vec1, vec2);
        final double denom = Vectors.euclideanNorm(vec1) * Vectors.euclideanNorm(vec2) + dampingFactor;
        if (Scalars.isZero(denom)) {
            return 0;
        } else {
            return dot / denom;
        }
    }

    @Override
    public boolean isSparse() {
        return true;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("cosine[d=%s]", dampingFactor);
    }
}
