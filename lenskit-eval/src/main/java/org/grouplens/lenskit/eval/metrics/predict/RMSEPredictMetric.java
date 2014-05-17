/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import static java.lang.Math.sqrt;

/**
 * Evaluate a recommender's prediction accuracy with RMSE.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RMSEPredictMetric extends AbstractMetric<RMSEPredictMetric.Context, RMSEPredictMetric.AggregateResult, RMSEPredictMetric.UserResult> {
    private static final Logger logger = LoggerFactory.getLogger(RMSEPredictMetric.class);

    public RMSEPredictMetric() {
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
        double sse = 0;
        int n = 0;
        for (VectorEntry e : predictions.fast()) {
            if (Double.isNaN(e.getValue())) {
                continue;
            }

            double err = e.getValue() - ratings.get(e.getKey());
            sse += err * err;
            n++;
        }
        if (n > 0) {
            double rmse = sqrt(sse / n);
            context.addUser(n, sse, rmse);
            return new UserResult(rmse);
        } else {
            return null;
        }
    }

    @Override
    protected AggregateResult getTypedResults(Context context) {
        return context.finish();
    }

    public static class UserResult {
        @ResultColumn("RMSE")
        public final double mae;

        public UserResult(double err) {
            mae = err;
        }
    }

    public static class AggregateResult {
        @ResultColumn("RMSE.ByUser")
        public final double userRMSE;
        @ResultColumn("RMSE.ByRating")
        public final double globalRMSE;

        public AggregateResult(double uerr, double gerr) {
            userRMSE = uerr;
            globalRMSE = gerr;
        }
    }

    public class Context {
        private double totalSSE = 0;
        private double totalRMSE = 0;
        private int nratings = 0;
        private int nusers = 0;

        private void addUser(int n, double sse, double rmse) {
            totalSSE += sse;
            totalRMSE += rmse;
            nratings += n;
            nusers += 1;
        }

        public AggregateResult finish() {
            if (nratings > 0) {
                double v = sqrt(totalSSE / nratings);
                logger.info("RMSE: {}", v);
                return new AggregateResult(totalRMSE / nusers, v);
            } else {
                return null;
            }
        }
    }
}
