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

import static java.lang.Math.abs;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import org.grouplens.lenskit.tablewriter.TableWriter;
import org.grouplens.lenskit.tablewriter.TableWriterBuilder;
import org.grouplens.lenskit.vector.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluate a recommender's predictions by Mean Absolute Error. In general, prefer
 * RMSE ({@link RMSEEvaluator}) to MAE.
 *
 * <p>This evaluator computes two variants of MAE. The first is <emph>by-rating</emph>,
 * where the absolute error is averaged over all predictions. The second is
 * <emph>by-user</emph>, where the MAE is computed per-user and then averaged
 * over all users.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MAEEvaluator implements PredictionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(MAEEvaluator.class);

    int colMAE;

    @Override
    public Accumulator makeAccumulator() {
        return new Accum();
    }

    @Override
    public void setup(TableWriterBuilder builder) {
        colMAE = builder.addColumn("MAE");
    }

    class Accum implements Accumulator {
        private double totalError = 0;
        private int nratings = 0;

        @Override
        public void evaluatePredictions(long user, SparseVector ratings,
                                        SparseVector predictions) {
            double err = 0;
            int n = 0;
            for (Long2DoubleMap.Entry e: predictions.fast()) {
                if (Double.isNaN(e.getDoubleValue())) continue;

                err += abs(e.getDoubleValue() - ratings.get(e.getLongKey()));
                n++;
            }
            totalError += err;
            nratings += n;
        }

        @Override
        public void finalize(TableWriter writer) {
            double v = totalError / nratings;
            logger.info("MAE: {}", v);
            writer.setValue(colMAE, v);
        }

    }
}
