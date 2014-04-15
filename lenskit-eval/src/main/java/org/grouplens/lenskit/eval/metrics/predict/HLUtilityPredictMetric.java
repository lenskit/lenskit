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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HLUtilityPredictMetric extends AbstractMetric<MeanAccumulator, HLUtilityPredictMetric.Result, HLUtilityPredictMetric.Result> {
    private static final Logger logger = LoggerFactory.getLogger(HLUtilityPredictMetric.class);

    private double alpha;

    public HLUtilityPredictMetric(double newAlpha) {
        super(Result.class, Result.class);
        alpha = newAlpha;
    }

    public HLUtilityPredictMetric() {
        this(5);
    }

    @Override
    public MeanAccumulator createContext(Attributed algo, TTDataSet ds, Recommender rec) {
        return new MeanAccumulator();
    }

    double computeHLU(LongList items, SparseVector values) {
        double utility = 0;
        int rank = 0;
        LongIterator itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            final double v = values.get(itemIterator.nextLong());
            rank++;
            utility += v / Math.pow(2, (rank - 1) / (alpha - 1));
        }
        return utility;
    }

    public static class Result {
        @ResultColumn("HLUtility")
        public final double utility;

        public Result(double util) {
            utility = util;
        }
    }

    @Override
    public Result doMeasureUser(TestUser user, MeanAccumulator context) {
        SparseVector predictions = user.getPredictions();
        if (predictions == null) {
            return null;
        }

        SparseVector ratings = user.getTestRatings();
        LongList ideal = ratings.keysByValue(true);
        LongList actual = predictions.keysByValue(true);
        double idealUtility = computeHLU(ideal, ratings);
        double actualUtility = computeHLU(actual, ratings);
        double u = actualUtility / idealUtility;

        context.add(u);
        return new Result(u);
    }

    @Override
    protected Result getTypedResults(MeanAccumulator context) {
        return new Result(context.getMean());
    }
}
