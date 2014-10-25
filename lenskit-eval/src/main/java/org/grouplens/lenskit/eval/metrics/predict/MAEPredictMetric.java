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
package org.grouplens.lenskit.eval.metrics.predict;

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;

/**
 * Evaluate a recommender's predictions by Mean Absolute Error. In general, prefer
 * RMSE ({@link RMSEPredictMetric}) to MAE.
 *
 * <p>This evaluator computes two variants of MAE. The first is <em>by-rating</em>,
 * where the absolute error is averaged over all predictions. The second is
 * <em>by-user</em>, where the MAE is computed per-user and then averaged
 * over all users.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MAEPredictMetric extends AbstractMetric<MAEPredictMetric.Context, MAEPredictMetric.AggregateResult, MAEPredictMetric.UserResult> {
    private static final Logger logger = LoggerFactory.getLogger(MAEPredictMetric.class);

    public MAEPredictMetric() {
        super(AggregateResult.class, UserResult.class);
    }

    @Override
    public Context createContext(Attributed algo, TTDataSet ds, Recommender rec) {
        return new Context();
    }

    @Override
    public UserResult doMeasureUser(TestUser user, Context context) {
        SparseVector ratings = user.getTestRatings();
        SparseVector predictions = user.getPredictions();
        if (predictions == null) {
            return null;
        }
        double err = 0;
        int n = 0;
        for (VectorEntry e : predictions) {
            if (Double.isNaN(e.getValue())) {
                continue;
            }

            err += abs(e.getValue() - ratings.get(e.getKey()));
            n++;
        }

        if (n > 0) {
            double mae = err / n;
            context.addUser(n, err, mae);
            return new UserResult(mae);
        } else {
            return null;
        }
    }

    @Override
    protected AggregateResult getTypedResults(Context context) {
        return context.finish();
    }

    public static class UserResult {
        @ResultColumn("MAE")
        public final double mae;

        public UserResult(double err) {
            mae = err;
        }
    }

    public static class AggregateResult {
        @ResultColumn("MAE.ByUser")
        public final double userMAE;
        @ResultColumn("MAE.ByRating")
        public final double globalMAE;

        public AggregateResult(double umae, double gmae) {
            userMAE = umae;
            globalMAE = gmae;
        }
    }

    public class Context {
        private double totalError = 0;
        private double totalUserError = 0;
        private int nratings = 0;
        private int nusers = 0;

        public void addUser(int nr, double sae, double mae) {
            totalError += sae;
            totalUserError += mae;
            nratings += nr;
            nusers += 1;
        }

        public AggregateResult finish() {
            if (nratings > 0) {
                double v = totalError / nratings;
                double uv = totalUserError / nusers;
                logger.info("MAE: {}", v);
                return new AggregateResult(uv, v);
            } else {
                return null;
            }
        }

    }
}
