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
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.jcip.annotations.ThreadSafe;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;

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
 * Shapira, B., and Kantor, P. (eds.), <i>Recommender Systems Handbook</i>,
 * Springer. 2010, pp. 107-144.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@ThreadSafe
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
    public double similarity(Long2DoubleMap vec1, Long2DoubleMap vec2) {
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
        LongSortedSet commonKeys = LongUtils.setIntersect(vec1.keySet(), vec2.keySet());
        int n = commonKeys.size();
        if (n == 0) {
            return 0;
        }

        double sum1 = 0;
        double sum2 = 0;

        for (LongIterator iter = commonKeys.iterator(); iter.hasNext();) {
            long k = iter.nextLong();
            sum1 += vec1.get(k);
            sum2 += vec2.get(k);
        }


        final double mu1 = sum1 / n;
        final double mu2 = sum2 / n;

        double var1 = 0;
        double var2 = 0;
        double dot = 0;
        int nCoratings = 0;

        for (LongIterator iter = commonKeys.iterator(); iter.hasNext();) {
            long k = iter.nextLong();
            final double v1 = vec1.get(k) - mu1;
            final double v2 = vec2.get(k) - mu2;
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
