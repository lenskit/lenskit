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
package org.lenskit.eval.traintest;

import org.lenskit.api.Recommender;

import java.util.List;
import java.util.Set;

/**
 * Interface for evaluation tasks.  Each evaluation task performs some task with the trained model and measures the
 * results.  Performing a task on a recommender trained over a particular data set results is called a measurement.
 *
 * @see TrainTestExperiment
 */
public interface EvalTask {
    /**
     * Get columns that will go in the aggregate output file.
     *
     * @return The list of column names that this task will contribute to the aggregate output file.
     */
    List<String> getGlobalColumns();

    /**
     * Get columns that will go in the per-user output file.
     *
     * @return The list of column names that this task will contribute to the per-user output file.
     */
    List<String> getUserColumns();

    /**
     * Get the root types required by this evaluation.
     * @return The root types required by this evaluation.
     */
    Set<Class<?>> getRequiredRoots();

    /**
     * Do initial setup for this eval task.  This should create any per-task output files, etc.
     *
     * @param outputLayout The output layout for experiment results.
     */
    void start(ExperimentOutputLayout outputLayout);

    /**
     * Finalize this eval task.  This should finish writing and close any per-task output files, etc.
     */
    void finish();

    /**
     * Set up a measurement of a single recommender.
     *
     * @param algorithm The algorithm being evaluated.
     * @param dataSet The data set being evaluated.
     * @param rec The recommender to measure.
     * @return A condition evaluator that will measure the recommender's performance on the algorithm and data set.
     */
    ConditionEvaluator createConditionEvaluator(AlgorithmInstance algorithm, DataSet dataSet, Recommender rec);
}
