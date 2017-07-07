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
import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
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
        LongArrays.quickSort(ids, new AbstractLongComparator() {
            @Override
            public int compare(long k1, long k2) {
                return Doubles.compare(vec.get(k2), vec.get(k1));
            }
        });

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
