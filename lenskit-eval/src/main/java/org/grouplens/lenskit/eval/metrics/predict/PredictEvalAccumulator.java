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
