package org.grouplens.lenskit.eval.metrics.predict;

import org.grouplens.lenskit.vectors.SparseVector;

/**
* @author Michael Ekstrand
*/
public interface PredictEvalAccumulator {
    /**
     * Evaluate the predictions for a user.
     * @param user The ID of the user currenting being tested.
     * @param ratings The user's rating vector over the test set.
     * @param predictions The user's prediction vector over the test set.
     * @return The results of this user's evaluation, to be emitted in the per-user table
     * (if one is configured). The output can be {@code null} if the user could not be
     * evaluated.
     */
    String[] evaluatePredictions(long user, SparseVector ratings, SparseVector predictions);

    /**
     * Finalize the evaluation and return the final values.
     * @return The column values for the final evaluation.
     */
    String[] finalResults();
}
