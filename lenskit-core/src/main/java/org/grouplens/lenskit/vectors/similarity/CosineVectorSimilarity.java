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
package org.grouplens.lenskit.vectors.similarity;

import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Cosine similarity for vectors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
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
        dampingFactor = damping;
    }

    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        final double dot = vec1.dot(vec2);
        final double denom = vec1.norm() * vec2.norm() + dampingFactor;
        if (denom == 0) {
            return 0;
        }

        return dot / denom;
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
