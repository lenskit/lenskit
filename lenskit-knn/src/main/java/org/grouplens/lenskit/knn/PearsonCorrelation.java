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

import static java.lang.Math.sqrt;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.util.Iterator;

import org.grouplens.lenskit.knn.params.SimilarityDamping;
import org.grouplens.lenskit.util.SymmetricBinaryFunction;
import org.grouplens.lenskit.vector.SparseVector;

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
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class PearsonCorrelation implements OptimizableVectorSimilarity<SparseVector>, SymmetricBinaryFunction {
    private static final long serialVersionUID = 4116492312815769666L;

    private final double shrinkage;

    public PearsonCorrelation() {
        this(0);
    }

    public PearsonCorrelation(@SimilarityDamping double s) {
        shrinkage = s;
    }

    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        // First check for empty vectors - then we can assume at least one element
        if (vec1.isEmpty() || vec2.isEmpty())
            return 0;

        /*
         * Basic similarity: walk in parallel across the two vectors, computing
         * the dot product and simultaneously computing the variance within each
         * vector of the items also contained in the other vector.  Pearson
         * correlation only considers items shared by both vectors; other items
         * aren't entirely discarded for the purpose of similarity computation.
         */
        final double mu1 = vec1.mean();
        final double mu2 = vec2.mean();

        double var1 = 0;
        double var2 = 0;
        double dot = 0;
        int nCoratings = 0; // number of common items rated
        Iterator<Long2DoubleMap.Entry> it1 = vec1.fastIterator();
        Iterator<Long2DoubleMap.Entry> it2 = vec2.fastIterator();
        Long2DoubleMap.Entry e1 = it1.next();
        Long2DoubleMap.Entry e2 = it2.next();
        do {
            /* Do one step of the parallel walk.  If the two entries have the
             * same key, add to the accumulators and advance both.  Otherwise,
             * advance the one further back to try to catch up.
             */
            // TODO Fix this loop to have cleaner hasNext/next pairs
            if (e1.getLongKey() == e2.getLongKey()) {
                final double v1 = e1.getDoubleValue() - mu1;
                final double v2 = e2.getDoubleValue() - mu2;
                var1 += v1 * v1;
                var2 += v2 * v2;
                dot += v1 * v2;
                nCoratings += 1;
                if (it1.hasNext())
                    e1 = it1.next();
                if (it2.hasNext())
                    e2 = it2.next();
            } else if (e1.getLongKey() < e2.getLongKey()) {
                if (it1.hasNext())
                    e1 = it1.next();
            } else {
                if (it2.hasNext())
                    e2 = it2.next();
            }
        } while (it1.hasNext() && it2.hasNext());

        if (nCoratings == 0)
            return 0;
        else
            return dot / (sqrt(var1 * var2) + shrinkage);
    }
}
