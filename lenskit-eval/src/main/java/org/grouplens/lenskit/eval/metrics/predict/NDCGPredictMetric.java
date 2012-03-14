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
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.log;

/**
 * Evaluate a recommender's predictions with normalized discounted cumulative gain.
 *
 * <p>This is a prediction evaluator that uses base-2 nDCG to evaluate recommender
 * accuracy. The items are ordered by predicted preference and the nDCG is
 * computed using the user's real rating as the gain for each item. Doing this
 * only over the queried items, rather than in the general recommend condition,
 * avoids penalizing recommenders for recommending items that would be better
 * if the user had known about them and provided ratings (e.g., for doing their
 * job).
 *
 * <p>nDCG is computed per-user and then averaged over all users.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class NDCGPredictMetric extends AbstractPredictEvalMetric {
    private static final Logger logger = LoggerFactory.getLogger(NDCGPredictMetric.class);
    private static final String[] COLUMNS = { "nDCG" };

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

    /**
     * Compute the DCG of a list of items with respect to a value vector.
     */
    static double computeDCG(LongList items, SparseVector values) {
        final double lg2 = log(2);

        double gain = 0;
        int rank = 0;

        LongIterator iit = items.iterator();
        while (iit.hasNext()) {
            final long item = iit.nextLong();
            final double v = values.get(item);
            rank++;
            if (rank < 2)
                gain += v;
            else
                gain += v * lg2 / log(rank);
        }

        return gain;
    }

    class Accum implements PredictEvalAccumulator {
        double total = 0;
        int nusers = 0;

        @Override
        public String[] evaluatePredictions(long user, SparseVector ratings,
                                        SparseVector predictions) {
            LongList ideal = ratings.keysByValue(true);
            LongList actual = predictions.keysByValue(true);
            double idealGain = computeDCG(ideal, ratings);
            double gain = computeDCG(actual, ratings);
            double score = gain / idealGain;
            total += score;
            nusers += 1;
            return new String[]{Double.toString(score)};
        }

        @Override
        public String[] finalResults() {
            double v = total / nusers;
            logger.info("nDCG: {}", v);
            return new String[]{ Double.toString(v) };
        }
    }
}
