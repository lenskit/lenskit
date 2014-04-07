/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.metrics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A simple metric base class that tracks the current evaluation.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public abstract class AbstractMetric<A extends MetricAccumulator> implements Metric<A> {
    /**
     * Make a user result row. This expands it to the length of the user columns, inserting
     * {@code null}s as needed.
     * @return The result row, the same length as {@link #getUserColumnLabels()}.
     */
    protected List<Object> userRow(Object... results) {
        int len = getUserColumnLabels().size();
        Preconditions.checkArgument(results.length <= len, "too many results");;
        List<Object> row = Lists.newArrayListWithCapacity(len);
        Collections.addAll(row, results);
        while (row.size() < len) {
            row.add(null);
        }
        return row;
    }

    /**
     * Make a final aggregate result row. This expands it to the length of the columns, inserting
     * {@code null}s as needed.
     * @return The result row, the same length as {@link #getColumnLabels()}.
     */
    protected List<Object> finalRow(Object... results) {
        int len = getColumnLabels().size();
        Preconditions.checkArgument(results.length <= len, "too many results");;
        List<Object> row = Lists.newArrayListWithCapacity(len);
        Collections.addAll(row, results);
        while (row.size() < len) {
            row.add(null);
        }
        return row;
    }

    /**
     * Close the metric.  Many metrics do not need to be closed, so this implementation is a no-op.
     *
     * @throws IOException if there is an error closing the metric.
     */
    @Override
    public void close() throws IOException {}

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

    /**
     * An accumulator that returns a pre-computed result. Used for metrics where the real computation
     * happens in {@link Metric#createAccumulator(org.grouplens.lenskit.eval.Attributed, org.grouplens.lenskit.eval.data.traintest.TTDataSet, org.grouplens.lenskit.Recommender)}.
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
}
