/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
