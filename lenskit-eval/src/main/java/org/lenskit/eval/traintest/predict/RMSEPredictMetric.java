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
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.data.events.Event;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static java.lang.Math.sqrt;

/**
 * Evaluate a recommender's prediction accuracy with RMSE.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RMSEPredictMetric extends PredictMetric<RMSEPredictMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(RMSEPredictMetric.class);

    public RMSEPredictMetric() {
        super(UserResult.class, AggregateResult.class);
    }

    @Override
    public Context createContext(Attributed algo, TTDataSet ds, Recommender rec) {
        return new Context();
    }

    @Nonnull
    @Override
    public MetricResult measureUser(UserHistory<Event> user, Long2DoubleMap ratings, ResultMap predictions, Context context) {
        double sse = 0;
        int n = 0;
        for (Result e : predictions) {
            if (!e.hasScore()) {
                continue;
            }

            double err = e.getScore() - ratings.get(e.getId());
            sse += err * err;
            n++;
        }
        if (n > 0) {
            double rmse = sqrt(sse / n);
            context.addUser(n, sse, rmse);
            return new UserResult(rmse);
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
        @MetricColumn("RMSE")
        public final double rmse;

        public UserResult(double err) {
            rmse = err;
        }
    }

    static class AggregateResult extends TypedMetricResult {
        @MetricColumn("RMSE.ByUser")
        public final double userRMSE;
        @MetricColumn("RMSE.ByRating")
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
