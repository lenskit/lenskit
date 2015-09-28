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

import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
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
 * Simple evaluator that records user, rating and prediction counts and computes
 * recommender coverage over the queried items.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CoveragePredictMetric extends PredictMetric<CoveragePredictMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(CoveragePredictMetric.class);

    public CoveragePredictMetric() {
        super(Coverage.class, AggregateCoverage.class);
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, org.lenskit.api.Recommender recommender) {
        return new Context();
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, ResultMap predictions, Context context) {
        int n = user.getTestRatings().size();
        int good = predictions.size();
        assert LongUtils.setDifference(LongUtils.asLongSet(predictions.keySet()),
                                       user.getTestRatings().keySet())
                        .isEmpty();
        context.addUser(n, good);
        return new Coverage(n, good);
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return new AggregateCoverage(context.nusers, context.npreds, context.ngood);
    }

    public static class Coverage extends TypedMetricResult {
        @MetricColumn(value="NAttempted", order=1)
        public final int nattempted;
        @MetricColumn(value="NGood", order=2)
        public final int ngood;

        private Coverage(int na, int ng) {
            nattempted = na;
            ngood = ng;
        }

        @ResultColumn(value="Coverage", order=3)
        public Double getCoverage() {
            if (nattempted > 0) {
                return ((double) ngood) / nattempted;
            } else {
                return null;
            }
        }
    }

    public static class AggregateCoverage extends Coverage {
        @MetricColumn(value="NUsers", order=0)
        public final int nusers;

        private AggregateCoverage(int nu, int na, int ng) {
            super(na, ng);
            nusers = nu;
        }
    }

    public class Context {
        private int npreds = 0;
        private int ngood = 0;
        private int nusers = 0;

        private void addUser(int np, int ng) {
            npreds += np;
            ngood += ng;
            nusers += 1;
        }
    }
}
