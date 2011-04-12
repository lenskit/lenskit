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
package org.grouplens.lenskit.knn;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.SymmetricBinaryFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similarity function using cosine similarity.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CosineSimilarity
    implements OptimizableVectorSimilarity<SparseVector>, SymmetricBinaryFunction {
    private static final Logger logger = LoggerFactory.getLogger(CosineSimilarity.class);

    private final double dampingFactor;

    public CosineSimilarity() {
        this(0.0);
    }

    public CosineSimilarity(double dampingFactor) {
        this.dampingFactor = dampingFactor;
        logger.debug("Using damping factor {}", dampingFactor);
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.Similarity#similarity(java.lang.Object, java.lang.Object)
     */
    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        final double dot = vec1.dot(vec2);
        final double denom = vec1.norm() * vec2.norm() + dampingFactor;
        if (denom == 0)
            return 0;

        return dot / denom;
    }
}
