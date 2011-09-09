/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.eval.predict;

import org.grouplens.lenskit.tablewriter.TableWriter;
import org.grouplens.lenskit.tablewriter.TableWriterBuilder;
import org.grouplens.lenskit.vector.SparseVector;


/**
 * Interface for prediction accuracy evalutors.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface PredictionEvaluator {
    /**
     * Create a result accumulator for a single row for this evaluation.
     * @return The result accumulator for aggregating prediction results.
     */
    Accumulator makeAccumulator();

    /**
     * Set the table writer builder used for the evaluator. The evaluator should
     * get the columns of its output fields here.
     */
    void setup(TableWriterBuilder twbuilder);

    public static interface Accumulator {
        /**
         * Evaluate the predictions for a user.
         * @param user
         * @param ratings
         * @param predictions
         */
        void evaluatePredictions(long user, SparseVector ratings, SparseVector predictions);

        /**
         * Finalize the evaluation and write the output to the current row of a table.
         * @param writer The table writer to use.
         */
        void finalize(TableWriter writer);
    }
}