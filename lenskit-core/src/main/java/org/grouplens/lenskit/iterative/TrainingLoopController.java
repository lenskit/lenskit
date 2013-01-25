package org.grouplens.lenskit.iterative;

/**
 * Training Loop controller for iterative updates
 */
public interface TrainingLoopController {
    /**
     *
     * @param error The error of the last iteration completed (use {Gustav Lindqvist Double#POSITIVE_INFINITY} before the first iteration).
     * @return {@code false} if the computation is finished.
     */
    boolean keepTraining(double error);

    /**
     *
     * @return the number of iterations done so far.
     */
    int getIterationCount();
}
