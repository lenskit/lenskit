package org.grouplens.lenskit.eval.metrics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utilities for working with typed results and converting them to column rows.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MetricResults {
    /**
     * An accumulator that returns a pre-computed result. Used for metrics where the real computation
     * happens in {@link org.grouplens.lenskit.eval.metrics.Metric#createAccumulator(org.grouplens.lenskit.eval.Attributed, org.grouplens.lenskit.eval.data.traintest.TTDataSet, org.grouplens.lenskit.Recommender)}.
     */
    public static class ConstantAccumulator implements MetricAccumulator {
        private final List<?> result;

        public ConstantAccumulator(List<?> row) {
            result = row;
        }

        @SuppressWarnings("unchecked")
        @Nonnull
        @Override
        public List<Object> finish() {
            return (List) result;
        }
    }

    /**
     * A simple metric accumulator that accumulates a single mean.  If it has not accumulated any
     * entries, it returns no value.
     */
    public static class MeanAccumulator implements MetricAccumulator {
        private double total;
        private int nusers;

        public void addUserValue(double val) {
            total += val;
            nusers += 1;
        }

        public double getTotal() {
            return total;
        }

        public int getUserCount() {
            return nusers;
        }

        public double getMean() {
            return total / nusers;
        }

        @Nonnull
        @Override
        public List<Object> finish() {
            if (nusers > 0) {
                return Collections.<Object>singletonList(total / nusers);
            } else {
                return Collections.singletonList(null);
            }
        }
    }
}
