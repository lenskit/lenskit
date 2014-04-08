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

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.traintest.TestUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.List;

/**
 * Base interface for metrics, which are used by evaluations to measure their results.  Metrics may
 * have additional resources backing them, such as extra output files, and therefore must be closed.
 *
 * <p>
 * A metric can define a <em>metric accumulator</em>, created by {@link #createAccumulator(Attributed, TTDataSet, Recommender)},
 * that is used to accumulate the results of different users in a single experiment.
 *
 * @param <A> The type of accumulator used by this metric.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10 (rewritten in 2.1)
 */
public interface Metric<A> extends Closeable {
    /**
     * Get labels for the aggregate columns output by this evaluator.
     *
     * @return The labels for this evaluator's output, used as column headers when
     *         outputting the results table.
     */
    List<String> getColumnLabels();

    /**
     * Get labels for the per-user columns output by this evaluator.
     *
     * @return The labels for this evaluator's per-user output, used as column headers
     *         when outputting the results table.
     * @see #measureUser(TestUser, Object)
     */
    List<String> getUserColumnLabels();

    /**
     * Create the accumulator for a single experiment (algorithm/data set pair).
     *
     *
     * @param algorithm The algorithm.
     * @param dataSet   The data set.
     * @param recommender The LensKit recommender, if applicable.  This can be null for an external
     *                    algorithm that does not provide a LensKit recommender.
     * @return The accumulator.  This will be passed to {@link #measureUser(TestUser, Object)}. If
     * the metric does not accumulate any results, this method can return {@code null}.
     */
    @Nullable
    A createAccumulator(Attributed algorithm, TTDataSet dataSet, Recommender recommender);

    /**
     * Measure a user in the evaluation.
     * @param user The user to evaluate.
     * @param accumulator The accumulator for this experiment.
     * @return The table rows resulting from this user's measurement.
     */
    @Nonnull
    List<Object> measureUser(TestUser user, A accumulator);

    /**
     * Get the aggregate results from an accumulator.
     * @param accum An accumulator.
     * @return The aggregate results from the accumulator.
     */
    @Nonnull
    List<Object> getResults(A accum);
}
