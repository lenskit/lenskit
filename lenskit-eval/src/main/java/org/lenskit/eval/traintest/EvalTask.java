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
package org.lenskit.eval.traintest;

import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;

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
     * @param rec The recommender engine that will be measured.
     * @return A condition evaluator that will measure the recommender's performance on the algorithm and data set.
     */
    ConditionEvaluator createConditionEvaluator(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine rec);
}
