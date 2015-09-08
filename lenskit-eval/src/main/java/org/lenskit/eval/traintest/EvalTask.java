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
