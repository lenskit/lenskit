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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.eval.traintest.TestUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * A simple metric base class that tracks the current evaluation.
 *
 * @param <A> The accumulator type.
 * @param <U> The type of per-user results.
 * @param <G> The type of global results.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public abstract class AbstractMetric<A, G, U> implements Metric<A> {
    private final ResultConverter<G> aggregateConverter;
    private final ResultConverter<U> userConverter;

    protected AbstractMetric(Class<G> aggregate, Class<U> user) {
        aggregateConverter = ResultConverter.create(aggregate);
        userConverter = ResultConverter.create(user);
    }

    /**
     * A prefix to be applied to column names.  If non-{@code null}, this prefix will be applied and
     * separated with a period.
     * @return The prefix to apply to column names.
     */
    protected String getPrefix() {
        return null;
    }

    /**
     * A suffix to be applied to column names.  If non-{@code null}, this suffix will be applied and
     * separated with a period.
     * @return The suffix to apply to column names.
     */
    protected String getSuffix() {
        return null;
    }

    @Override
    public List<String> getColumnLabels() {
        return Lists.transform(aggregateConverter.getColumnLabels(), new HeaderTransform());
    }

    @Override
    public List<String> getUserColumnLabels() {
        return Lists.transform(userConverter.getColumnLabels(), new HeaderTransform());
    }

    @Nonnull
    @Override
    public List<Object> measureUser(TestUser user, A accumulator) {
        return userConverter.getColumns(doMeasureUser(user, accumulator));
    }

    /**
     * Measure a user with typed results.
     * @param user The user to measure.
     * @param accum The accumulator.
     * @return The results of measuring the user, or {@code null} to emit NAs for the user.
     */
    protected abstract U doMeasureUser(TestUser user, A accum);

    @Nonnull
    @Override
    public List<Object> getResults(A accum) {
        return aggregateConverter.getColumns(getTypedResults(accum));
    }

    /**
     * Get the typed results from an accumulator.
     * @param accum The accumulator.
     * @return The results accumulated for this experiment, or {@code null} to emit NAs.
     */
    protected abstract G getTypedResults(A accum);

    /**
     * Close the metric.  Many metrics do not need to be closed, so this implementation is a no-op.
     *
     * @throws IOException if there is an error closing the metric.
     */
    @Override
    public void close() throws IOException {}

    private class HeaderTransform implements Function<String,String> {
        @Nullable
        @Override
        public String apply(@Nullable String input) {
            StringBuilder sb = new StringBuilder();
            String part = getPrefix();
            if (part != null) {
                sb.append(part).append(".");
            }
            sb.append(input);
            part = getSuffix();
            if (part != null) {
                sb.append(part).append(".");
            }
            return sb.toString();
        }
    }
}
