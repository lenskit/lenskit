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

import static java.lang.Math.log;
import static java.lang.Math.sqrt;
import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.tablewriter.TableWriter;
import org.grouplens.lenskit.tablewriter.TableWriterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class NDCGEvaluator implements PredictionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(NDCGEvaluator.class);
    
    int colNDCG;

    @Override
    public Accumulator makeAccumulator() {
        return new Accum();
    }

    @Override
    public void setup(TableWriterBuilder builder) {
        colNDCG = builder.addColumn("nDCG");
    }
    
    /**
     * Sort the keys of a vector in decreasing order by value.
     * @param vector The vector to query.
     * @return The {@link SparseVector#keySet()} of <var>vector</var>, sorted
     * by value.
     */
    static LongList sortKeys(final SparseVector vector) {
        long[] items = vector.keySet().toLongArray();
        for (int i = 0; i < items.length; i++) {
            if (Double.isNaN(vector.get(items[i])))
                throw new RuntimeException("Unexpected NaN");
        }
        LongArrays.quickSort(items, new AbstractLongComparator() {
            @Override
            public int compare(long k1, long k2) {
                return Double.compare(vector.get(k2), vector.get(k1));
            }
        });
        return LongArrayList.wrap(items);
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
    
    class Accum implements Accumulator {
        private double total = 0;
        private int nusers = 0;
        
        @Override
        public void evaluatePredictions(long user, SparseVector ratings,
                                        SparseVector predictions) {
            LongList ideal = sortKeys(ratings);
            LongList actual = sortKeys(predictions);
            double idealGain = computeDCG(ideal, ratings);
            double gain = computeDCG(actual, ratings);
            total += gain / idealGain;
            nusers += 1;
        }

        @Override
        public void finalize(TableWriter writer) {
            double v = total / nusers;
            logger.info("nDCG: {}", v);
            writer.setValue(colNDCG, v);
        }
        
    }
}
