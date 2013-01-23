package org.grouplens.lenskit.iterative;

/**
 * Training Loop controller for iterative updates
 */
public interface TrainingLoopController {
    /**
     * Query whether the computation should stop.
     *
     * @param error The root-mean-square error.
     * @return {@code false} if the computation is finished.
     */
    boolean keepTraining(double error);

    /**
     *
     * @return the number of iterations done so far.
     */
    int getIterationCount();
}
