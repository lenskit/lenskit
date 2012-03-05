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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;

/**
 * Evaluate a recommender's predictions by Mean Absolute Error. In general, prefer
 * RMSE ({@link RMSEPredictMetric}) to MAE.
 *
 * <p>This evaluator computes two variants of MAE. The first is <emph>by-rating</emph>,
 * where the absolute error is averaged over all predictions. The second is
 * <emph>by-user</emph>, where the MAE is computed per-user and then averaged
 * over all users.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MAEPredictMetric extends AbstractPredictEvalMetric {
    private static final Logger logger = LoggerFactory.getLogger(MAEPredictMetric.class);
    private static final String[] COLUMNS = { "MAE", "MAE.ByUser" };
    private static final String[] USER_COLUMNS = {"MAE"};

    @Override
    public PredictEvalAccumulator makeAccumulator(AlgorithmInstance algo, TTDataSet ds) {
        return new Accum();
    }

    @Override
    public String[] getColumnLabels() {
        return COLUMNS;
    }

    @Override
    public String[] getUserColumnLabels() {
        return USER_COLUMNS;
    }

    class Accum implements PredictEvalAccumulator {
        private double totalError = 0;
        private double totalUserError = 0;
        private int nratings = 0;
        private int nusers = 0;

        @Override
        public String[] evaluatePredictions(long user, SparseVector ratings,
                                        SparseVector predictions) {
            double err = 0;
            int n = 0;
            for (Long2DoubleMap.Entry e: predictions.fast()) {
                if (Double.isNaN(e.getDoubleValue())) continue;

                err += abs(e.getDoubleValue() - ratings.get(e.getLongKey()));
                n++;
            }

            if (n > 0) {
                totalError += err;
                nratings += n;
                double errRate = err / n;
                totalUserError += errRate;
                nusers += 1;
                return new String[]{Double.toString(errRate)};
            } else {
                return null;
            }
        }

        @Override
        public String[] finalResults() {
            double v = totalError / nratings;
            double uv = totalUserError / nusers;
            logger.info("MAE: {}", v);
            return new String[]{
            		Double.toString(v),
            		Double.toString(uv)
            };
        }

    }
}
