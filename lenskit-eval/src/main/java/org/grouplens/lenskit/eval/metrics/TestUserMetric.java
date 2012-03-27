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

import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.traintest.TrainTestEvalTask;

/**
 * Interface for prediction and recommendation accuracy evaluators. Evaluators
 * produce accumulators which in turn accumulate recommendation statistics, returning
 * aggregate error information in the {@link TestUserMetricAccumulator#finalResults()}
 * method.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @since 0.10
 */
public interface TestUserMetric extends Metric<TrainTestEvalTask> {
    /**
     * Create a result accumulator for a single row for this evaluation. The accumulator
     * will be passed the predictions for each user in turn, then asked for the results
     * from the evaluation to insert into the results table.
     * <p/>
     * One accumulator is created and used per evaluation (data set × algorithm).
     * <p/>
     * Individual accumulators do not need to be thread-safe, but it must
     * be possible to have multiple accumulators in separate threads in use concurrently.
     *
     * @param algorithm The algorithm to be evaluated.
     * @param dataSet The data set being evaluated — used if the evaluator needs something
     *           from it (such as the preference domain).
     * @return The result accumulator for aggregating prediction results over a single
     * evaluation.
     */
    TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algorithm, TTDataSet dataSet);
    
    /**
     * Get labels for the aggregate columns output by this evaluator.
     * @return The labels for this evaluator's output, used as column headers when
     * outputting the results table.
     */
    String[] getColumnLabels();

    /**
     * Get labels for the per-user columns output by this evaluator.
     * @return The labels for this evaluator's per-user output, used as column headers
     * when outputting the results table.
     * @see TestUserMetricAccumulator#evaluate(org.grouplens.lenskit.eval.traintest.TestUser)
     */
    String[] getUserColumnLabels();
}
