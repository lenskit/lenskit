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
package org.lenskit.eval.traintest.metrics;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.lenskit.api.Result;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A simple metric base class that tracks the current evaluation.
 *
 * @param <X> The context type.
 * @param <R> The type of per-result output.  This is a plain Java class that has fields or methods
 *           annotated with {@link MetricColumn} containing the aggregate output columns.
 * @param <G> The type of global output.  This is a plain Java class that has fields or methods
 *           annotated with {@link MetricColumn} containing the aggregate output columns.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public abstract class AbstractMetric<X, G, R> implements Metric<X> {
    private final MetricResultConverter<G> aggregateConverter;
    private final MetricResultConverter<R> resultConverter;

    protected AbstractMetric(Class<G> aggregate, Class<R> result) {
        aggregateConverter = MetricResultConverter.create(aggregate);
        resultConverter = MetricResultConverter.create(result);
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
    public List<String> getAggregateColumnLabels() {
        return Lists.transform(aggregateConverter.getColumnLabels(), new HeaderTransform());
    }

    @Override
    public List<String> getResultColumnLabels() {
        return Lists.transform(resultConverter.getColumnLabels(), new HeaderTransform());
    }

    @Nullable
    @Override
    public X createContext(Attributed algorithm, TTDataSet dataSet, Recommender recommender) {
        return null;
    }

    @Nonnull
    @Override
    public List<Object> getAggregateMeasurements(X context) {
        return aggregateConverter.getColumns(typedAggregateMeasurements(context));
    }

    @Override
    public List<Object> measureResult(long userId, Result result, X context) {
        // TODO Implement this method
        return null;
    }

    /**
     * Get the typed results from an accumulator.
     * @param context The context.
     * @return The results accumulated for this experiment, or {@code null} to emit NAs.
     */
    protected abstract G typedAggregateMeasurements(X context);

    private class HeaderTransform implements Function<String,String> {
        @Nullable
        @Override
        public String apply(@Nullable String input) {
            StringBuilder sb = new StringBuilder();
            sb.append(input);
            String part = getSuffix();
            if (part != null) {
                sb.append(".").append(part);
            }
            return sb.toString();
        }
    }
}
