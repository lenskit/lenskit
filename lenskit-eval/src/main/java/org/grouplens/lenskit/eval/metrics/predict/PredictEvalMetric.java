/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval.metrics.predict;

import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Interface for prediction accuracy evaluators. Evaluators produce accumulators
 * which in turn accumulate prediction accuracy, returning aggregate error
 * information in the {@link Accumulator#results()} method.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface PredictEvalMetric {
    /**
     * Create a result accumulator for a single row for this evaluation. The accumulator
     * will be passed the predictions for each user in turn, then asked for the results
     * from the evaluation to insert into the results table.
     * <p/>
     * One accumulator is created and used per evaluation (data set × algorithm).
     *
     * @param ds The data set being evaluated — used if the evaluator needs something
     *           from it (such as the preference domain).
     * @return The result accumulator for aggregating prediction results over a single
     * evaluation.
     */
    Accumulator makeAccumulator(TTDataSet ds);
    
    /**
     * Get labels for the columns output by this evaluator.
     * @return The labels for this evaluator's output, used as column headers when
     * outputting the results table.
     */
    String[] getColumnLabels();

    public static interface Accumulator {
        /**
         * Evaluate the predictions for a user.
         * @param user The ID of the user currenting being tested.
         * @param ratings The user's rating vector over the test set.
         * @param predictions The user's prediction vector over the test set.
         */
        void evaluatePredictions(long user, SparseVector ratings, SparseVector predictions);

        /**
         * Finalize the evaluation and return the final values.
         * @return The column values for the final evaluation.
         */
        String[] results();
    }
}
