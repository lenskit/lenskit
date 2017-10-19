/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.eval.traintest.predict;

import org.lenskit.api.RecommenderEngine;
import org.lenskit.api.ResultMap;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.lenskit.util.collections.LongUtils;
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
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
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

        @MetricColumn(value="Coverage", order=3)
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

        private synchronized void addUser(int np, int ng) {
            npreds += np;
            ngood += ng;
            nusers += 1;
        }
    }
}
