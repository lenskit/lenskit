/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

/**
 * Simple evaluator that records user, rating and prediction counts and computes
 * recommender coverage over the queried items.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CoveragePredictMetric extends AbstractMetric<CoveragePredictMetric.Context, CoveragePredictMetric.AggregateCoverage, CoveragePredictMetric.Coverage> {
    private static final Logger logger = LoggerFactory.getLogger(CoveragePredictMetric.class);

    public CoveragePredictMetric() {
        super(AggregateCoverage.class, Coverage.class);
    }

    @Override
    public Context createContext(Attributed algo, TTDataSet ds, Recommender rec) {
        return new Context();
    }

    @Override
    public Coverage doMeasureUser(TestUser user, Context context) {
        SparseVector ratings = user.getTestRatings();
        SparseVector predictions = user.getPredictions();
        if (predictions == null) {
            return null;
        }
        int n = 0;
        int good = 0;
        for (VectorEntry e : ratings.fast()) {
            n += 1;
            if (predictions.containsKey(e.getKey())) {
                good += 1;
            }
        }
        context.addUser(n, good);
        return new Coverage(n, good);
    }

    @Override
    protected AggregateCoverage getTypedResults(Context context) {
        return new AggregateCoverage(context.nusers, context.npreds, context.ngood);
    }

    public static class Coverage {
        @ResultColumn(value="NAttempted", order=1)
        public final int nattempted;
        @ResultColumn(value="NGood", order=2)
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
        @ResultColumn(value="NUsers", order=0)
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
