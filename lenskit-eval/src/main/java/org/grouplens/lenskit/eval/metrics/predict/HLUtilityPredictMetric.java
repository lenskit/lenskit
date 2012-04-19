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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HLUtilityPredictMetric extends AbstractTestUserMetric {
    private static final Logger logger = LoggerFactory.getLogger(HLUtilityPredictMetric.class);
    private static final String[] COLUMNS = { "HLUtility" };
    
    private double alpha;

    public HLUtilityPredictMetric(double newAlpha) {
        alpha = newAlpha;
    }

    public HLUtilityPredictMetric() {
        alpha = 5;
    }

    @Override
    public Accum makeAccumulator(AlgorithmInstance algo, TTDataSet ds) {
        return new Accum();
    }
    
    @Override
    public String[] getColumnLabels() {
        return COLUMNS;
    }

    @Override
    public String[] getUserColumnLabels() {
        return COLUMNS;
    }

    double computeHLU(LongList items, SparseVector values) {
        double utility = 0;
        int rank = 0;
        LongIterator itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            final double v = values.get(itemIterator.nextLong());
            rank++;
            utility += v/Math.pow(2,(rank-1)/(alpha-1));
        }
        return utility;
    }

    public class Accum implements TestUserMetricAccumulator {

        double total = 0;
        int nusers = 0;

        @Override
        public Object[] evaluate(TestUser user) {
            return evaluatePredictions(user.getTestRatings(), user.getPredictions());
        }

        Object[] evaluatePredictions(SparseVector ratings, SparseVector predictions) {
            LongList ideal = ratings.keysByValue(true);
            LongList actual = predictions.keysByValue(true);
            double idealUtility = computeHLU(ideal, ratings);
            double actualUtility = computeHLU(actual, ratings);
            double u = actualUtility/idealUtility;
            total += u;
            nusers++;
            return new Object[]{u};
        }

        @Override
        public Object[] finalResults() {
            double v = total/nusers;
            logger.info("HLU: {}", v);
            return new Object[]{v};
        }
    }
}
