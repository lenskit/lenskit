/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.lenskit.eval.traintest.predict;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Evaluate a recommender's prediction accuracy with MAE (Mean Absolute Error).
 */
public class MAEPredictMetric extends PredictMetric<MAEPredictMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(MAEPredictMetric.class);

    public MAEPredictMetric() {
        super(UserResult.class, AggregateResult.class);
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, org.lenskit.api.Recommender recommender) {
        return new Context();
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, ResultMap predictions, Context context) {
        Long2DoubleMap ratings = user.getTestRatings();
        double totalError = 0;
        int n = 0;
        for (Result e : predictions) {
            if (!e.hasScore()) {
                continue;
            }

            double err = e.getScore() - ratings.get(e.getId());
            totalError += Math.abs(err);
            n++;
        }
        if (n > 0) {
            double mae = totalError / n;
            context.addUser(n, totalError, mae);
            return new UserResult(mae);
        } else {
            return MetricResult.empty();
        }
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return context.finish();
    }

    static class UserResult extends TypedMetricResult {
        @MetricColumn("MAE")
        public final double mae;

        public UserResult(double err) {
            mae = err;
        }
    }

    static class AggregateResult extends TypedMetricResult {
        @MetricColumn("MAE.ByUser")
        public final double userMAE;
        @MetricColumn("MAE.ByRating")
        public final double globalMAE;

        public AggregateResult(double uerr, double gerr) {
            userMAE = uerr;
            globalMAE = gerr;
        }
    }

    public class Context {
        private double totalError = 0;
        private double totalMAE = 0;
        private int nratings = 0;
        private int nusers = 0;

        private void addUser(int n, double err, double mae) {
            totalError += err;
            totalMAE += mae;
            nratings += n;
            nusers += 1;
        }

        public AggregateResult finish() {
            if (nratings > 0) {
                double v = totalError / nratings;
                logger.info("RMSE: {}", v);
                return new AggregateResult(totalMAE / nusers, v);
            } else {
                return null;
            }
        }
    }
}
