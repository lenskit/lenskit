/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

package org.grouplens.lenskit.eval.predict;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.tablewriter.TableWriter;
import org.grouplens.lenskit.tablewriter.TableWriterBuilder;
import org.grouplens.lenskit.vector.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HLUtilityEvaluator implements PredictionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(HLUtilityEvaluator.class);
    private int colHLU;
    private double alpha;

    public HLUtilityEvaluator(double newAlpha) {
        alpha = newAlpha;
    }

    public HLUtilityEvaluator() {
        alpha = 5;
    }

    @Override
    public Accum makeAccumulator() {
        return new Accum();
    }

    @Override
    public void setup(TableWriterBuilder builder) {
        colHLU = builder.addColumn("HLUtility");
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

    public class Accum implements Accumulator {


        double total = 0;
        int nusers = 0;

        @Override
        public void evaluatePredictions(long user, SparseVector ratings, SparseVector predictions) {

            LongList ideal = ratings.keysByValue(true);
            LongList actual = predictions.keysByValue(true);
            double idealUtility = computeHLU(ideal, ratings);
            double actualUtility = computeHLU(actual, ratings);
            total += actualUtility/idealUtility;
            nusers++;
        }

        @Override
        public void finalize(TableWriter writer) {

            double v = total/nusers;
            logger.info("HLU: {}", v);
            writer.setValue(colHLU, v);
        }
    }
}
