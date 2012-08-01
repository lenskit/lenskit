/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.sqrt;

/**
 * Evaluate a recommender's prediction accuracy with RMSE.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RMSEPredictMetric extends AbstractTestUserMetric {
    private static final Logger logger = LoggerFactory.getLogger(RMSEPredictMetric.class);
    private static final String[] COLUMNS = { "RMSE.ByRating", "RMSE.ByUser" };
    private static final String[] USER_COLUMNS = {"RMSE"};

    @Override
    public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algo, TTDataSet ds) {
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

    class Accum implements TestUserMetricAccumulator {
        private double sse = 0;
        private double totalRMSE = 0;
        private int nratings = 0;
        private int nusers = 0;

        @Override
        public Object[] evaluate(TestUser user) {
            SparseVector ratings = user.getTestRatings();
            SparseVector predictions = user.getPredictions();
            double usse = 0;
            int n = 0;
            for (VectorEntry e: predictions.fast()) {
                if (Double.isNaN(e.getValue())) continue;

                double err = e.getValue() - ratings.get(e.getKey());
                usse += err * err;
                n++;
            }
            sse += usse;
            nratings += n;
            if (n > 0) {
                double rmse = sqrt(usse / n);
                totalRMSE += rmse;
                nusers ++;
                return new Object[]{rmse};
            } else {
                return null;
            }
        }

        @Override
        public Object[] finalResults() {
            double v = sqrt(sse / nratings);
            logger.info("RMSE: {}", v);
            return new Object[] {v,totalRMSE / nusers};
        }
    }
}
