/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

/**
 * Base interface for metrics, which are used by evaluations to measure their results.
 *
 * @param <E> The type of evaluation.
 * @author Michael Ekstrand
 * @since 0.10
 */
public interface Metric<E> {
    /**
     * Initialize the metric to accumulate evaluations for the specified evaluation.
     * This method is called before any accumulators are constructed. It is invalid
     * to start a metric twice without finishing it first.
     *
     * @param eval The evaluation this metric is in use for.
     */
    void startEvaluation(E eval);

    /**
     * Finish the evaluation, releasing any resources allocated for it. This
     * will be called after all algorithms and data sets are run through the accumulator.
     * After this method, the metric should be ready to be used on another evaluation.
     *
     * @review Is that really the behavior we want?
     */
    void finishEvaluation();
}
