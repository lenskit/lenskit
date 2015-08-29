package org.lenskit.eval.traintest;

import org.lenskit.api.Recommender;

import java.util.Set;

/**
 * Interface for evaluation tasks.  Each evaluation task performs some task with the trained model and measures the
 * results.  Performing a task on a recommender trained over a particular data set results is called a measurement.
 */
public interface EvalTask {
    /**
     * Get columns that will go in the aggregate output file.
     *
     * @return The (ordered) set of columns names that this task will contribute to the aggregate output file.
     */
    Set<String> getGlobalColumns();

    /**
     * Get columns that will go in the per-user output file.
     *
     * @return The (ordered) set of columns names that this task will contribute to the per-user output file.
     */
    Set<String> getUserColumns();

    /**
     * Set up a measurement of a single recommender.
     * @param rec The recommender to measure.
     * @return The measurement that will measure it.
     */
    Measurement startMeasurement(Recommender rec);
}
