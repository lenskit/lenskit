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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.Vectors;

import javax.inject.Inject;
import java.io.Serializable;

import static java.lang.Math.sqrt;

/**
 * Similarity function using Pearson correlation.
 *
 * <p>This class implements the Pearson correlation similarity function over
 * sparse vectors.  Only the items occurring in both vectors are considered when
 * computing the variance.
 *
 * <p>See Desrosiers, C. and Karypis, G., <i>A Comprehensive Survey of
 * Neighborhood-based Recommendation Methods</i>.  In Ricci, F., Rokach, L.,
 * Shapira, B., and Kantor, P. (eds.), <i>RecommenderEngine Systems Handbook</i>,
 * Springer. 2010, pp. 107-144.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class PearsonCorrelation implements VectorSimilarity, Serializable {
    private static final long serialVersionUID = 1L;

    private final double shrinkage;

    public PearsonCorrelation() {
        this(0);
    }

    @Inject
    public PearsonCorrelation(@SimilarityDamping double s) {
        Preconditions.checkArgument(s >= 0, "negative damping not allowed");
        shrinkage = s;
    }

    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        // First check for empty vectors - then we can assume at least one element
        if (vec1.isEmpty() || vec2.isEmpty()) {
            return 0;
        }

        /*
         * Basic similarity: walk in parallel across the two vectors, computing
         * the dot product and simultaneously computing the variance within each
         * vector of the items also contained in the other vector.  Pearson
         * correlation only considers items shared by both vectors; other items
         * are discarded for the purpose of similarity computation.
         */

        // first compute means of common items
        double sum1 = 0;
        double sum2 = 0;
        int n = 0;
        for (Pair<VectorEntry,VectorEntry> pair: Vectors.fastIntersect(vec1, vec2)) {
            sum1 += pair.getLeft().getValue();
            sum2 += pair.getRight().getValue();
            n += 1;
        }

        if (n == 0) {
            return 0;
        }

        final double mu1 = sum1 / n;
        final double mu2 = sum2 / n;

        double var1 = 0;
        double var2 = 0;
        double dot = 0;
        int nCoratings = 0;

        for (Pair<VectorEntry,VectorEntry> pair: Vectors.fastIntersect(vec1, vec2)) {
            final double v1 = pair.getLeft().getValue() - mu1;
            final double v2 = pair.getRight().getValue() - mu2;
            var1 += v1 * v1;
            var2 += v2 * v2;
            dot += v1 * v2;
            nCoratings += 1;
        }

        if (nCoratings == 0) {
            return 0;
        } else {
            return dot / (sqrt(var1 * var2) + shrinkage);
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
        return String.format("Pearson[d=%s]", shrinkage);
    }
}
