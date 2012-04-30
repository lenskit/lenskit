/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import static java.lang.Math.max;

import java.io.Serializable;

import javax.inject.Inject;

import org.grouplens.lenskit.knn.params.WeightThreshold;
import org.grouplens.lenskit.knn.params.WeightedSimilarity;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Apply significance weighting to a similarity function. The inner function is
 * specified with the {@link WeightedSimilarity} component, and the threshold
 * with the {@link WeightThreshold} parameter.
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
 * @see WeightedSimilarity
 * @see WeightThreshold
 */
public class SignificanceWeightedVectorSimilarity implements VectorSimilarity, Serializable {

    private static final long serialVersionUID = 1L;

    private final int threshold;
    private final VectorSimilarity delegate;

    @Inject
    public SignificanceWeightedVectorSimilarity(@WeightThreshold int thresh,
                                                @WeightedSimilarity VectorSimilarity sim) {
        threshold = thresh;
        delegate = sim;
    }

    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        double s = delegate.similarity(vec1, vec2);
        int n = vec1.countCommonKeys(vec2);
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

}
