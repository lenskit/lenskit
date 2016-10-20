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
package org.lenskit.transform.normalize;

import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.inject.Shareable;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;

/**
 * User vector normalizer that subtracts a user's baseline scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BaselineSubtractingUserVectorNormalizer extends AbstractUserVectorNormalizer {
    protected final ItemScorer baselineScorer;

    /**
     * Create a new baseline-subtracting normalizer with the given baseline.
     *
     * @param baseline The baseline scorer to use for normalization.
     */
    @Inject
    public BaselineSubtractingUserVectorNormalizer(@BaselineScorer ItemScorer baseline) {
        baselineScorer = baseline;
    }

    @Override
    public VectorTransformation makeTransformation(long user, SparseVector ratings) {
        return new Transformation(user);
    }

    @Override
    public InvertibleFunction<Long2DoubleMap, Long2DoubleMap> makeTransformation(long user, Long2DoubleMap vector) {
        return new Transformation(user);
    }

    private class Transformation implements VectorTransformation {
        private final long user;

        public Transformation(long u) {
            user = u;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            Map<Long,Double> base = baselineScorer.score(user, vector.keySet());
            Long2DoubleFunction bf = LongUtils.asLong2DoubleFunction(base);
            for (VectorEntry e: vector.view(VectorEntry.State.SET)) {
                vector.set(e, e.getValue() - bf.get(e.getKey()));
            }
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            Map<Long,Double> base = baselineScorer.score(user, vector.keySet());
            Long2DoubleFunction bf = LongUtils.asLong2DoubleFunction(base);
            for (VectorEntry e: vector.view(VectorEntry.State.SET)) {
                vector.set(e, e.getValue() + bf.get(e.getKey()));
            }
            return vector;
        }

        @Override
        public Long2DoubleMap unapply(Long2DoubleMap input) {
            if (input == null) return null;

            Map<Long,Double> base = baselineScorer.score(user, input.keySet());

            SortedKeyIndex idx = SortedKeyIndex.fromCollection(input.keySet());
            int n = idx.size();
            double[] values = new double[n];
            for (int i = 0; i < n; i++) {
                long k = idx.getKey(i);
                Double bp = base.get(k);
                double bpv = bp != null ? bp : 0;
                values[i] = input.get(idx.getKey(i)) + bpv;
            }

            return Long2DoubleSortedArrayMap.wrap(idx, values);
        }

        @Nullable
        @Override
        public Long2DoubleMap apply(@Nullable Long2DoubleMap input) {
            if (input == null) return null;

            Map<Long,Double> base = baselineScorer.score(user, input.keySet());

            SortedKeyIndex idx = SortedKeyIndex.fromCollection(input.keySet());
            int n = idx.size();
            double[] values = new double[n];
            for (int i = 0; i < n; i++) {
                long k = idx.getKey(i);
                Double bp = base.get(k);
                double bpv = bp != null ? bp : 0;
                values[i] = input.get(idx.getKey(i)) - bpv;
            }

            return Long2DoubleSortedArrayMap.wrap(idx, values);
        }

        @Override
        public double apply(long key, double value) {
            Result res = baselineScorer.score(user, key);
            return res != null ? value - res.getScore() : value;
        }

        @Override
        public double unapply(long key, double value) {
            Result res = baselineScorer.score(user, key);
            return res != null ? value + res.getScore() : value;
        }
    }

    @Override
    public String toString() {
        return String.format("[BaselineNorm: %s]", baselineScorer);
    }
}
