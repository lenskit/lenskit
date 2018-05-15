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
import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import net.jcip.annotations.ThreadSafe;
import org.lenskit.inject.Shareable;
import org.lenskit.util.math.Scalars;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Similarity function using Spearman rank correlation.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@ThreadSafe
public class SpearmanRankCorrelation implements VectorSimilarity, Serializable {
    private static final long serialVersionUID = 3023239202579332883L;

    private final PearsonCorrelation pearson;

    @Inject
    public SpearmanRankCorrelation(@SimilarityDamping double damping) {
        Preconditions.checkArgument(damping >= 0, "negative damping not allowed");
        pearson = new PearsonCorrelation(damping);
    }

    public SpearmanRankCorrelation() {
        this(0.0);
    }

    static Long2DoubleMap rank(final Long2DoubleMap vec) {
        long[] ids = vec.keySet().toLongArray();
        // sort ID set by value (decreasing)
        LongArrays.quickSort(ids, (k1, k2) -> Doubles.compare(vec.get(k2), vec.get(k1)));

        final int n = ids.length;
        final double[] values = new double[n];
        Long2DoubleMap rank = new Long2DoubleOpenHashMap(n);
        // assign ranks to each item
        for (int i = 0; i < n; i++) {
            rank.put(ids[i], i+1);
            values[i] = vec.get(ids[i]);
        }

        // average ranks for items with same values
        int i = 0;
        while (i < n) {
            int j;
            for (j = i + 1; j < n; j++) {
                // compare difference to 0 with tolerance - more robust
                if (!Scalars.isZero(values[j] - values[i])) {
                    break;
                }
            }
            if (j - i > 1) {
                double r2 = (rank.get(ids[i]) + rank.get(ids[j - 1])) / (j - i);
                for (int k = i; k < j; k++) {
                    rank.put(ids[k], r2);
                }
            }
            i = j;
        }

        // Make a sparse vector out of it
        return rank;
    }

    @Override
    public double similarity(Long2DoubleMap vec1, Long2DoubleMap vec2) {
        return pearson.similarity(rank(vec1), rank(vec2));
    }

    @Override
    public boolean isSparse() {
        return true;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }
}
