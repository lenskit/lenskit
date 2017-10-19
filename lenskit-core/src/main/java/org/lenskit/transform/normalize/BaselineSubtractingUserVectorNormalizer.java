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
package org.lenskit.transform.normalize;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.api.ItemScorer;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.annotation.Nullable;
import javax.inject.Inject;
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
    public InvertibleFunction<Long2DoubleMap, Long2DoubleMap> makeTransformation(long user, Long2DoubleMap vector) {
        return new Transformation(user);
    }

    private class Transformation implements VectorTransformation {
        private final long user;

        public Transformation(long u) {
            user = u;
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

    }

    @Override
    public String toString() {
        return String.format("[BaselineNorm: %s]", baselineScorer);
    }
}
