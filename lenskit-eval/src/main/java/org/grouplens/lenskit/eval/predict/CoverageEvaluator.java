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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import org.grouplens.lenskit.util.spi.ConfigAlias;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple evaluator that records user, rating and prediction counts and computes
 * recommender coverage over the queried items.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ConfigAlias("coverage")
public class CoverageEvaluator implements PredictionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(CoverageEvaluator.class);
    private static final String[] COLUMNS = {
        "NUsers", "NAttempted", "NGood", "Coverage"
    };

    @Override
    public Accumulator makeAccumulator() {
        return new Accum();
    }
    
    @Override
    public String[] getColumnLabels() {
        return COLUMNS;
    }

    class Accum implements Accumulator {
        private int npreds = 0;
        private int ngood = 0;
        private int nusers = 0;

        @Override
        public void evaluatePredictions(long user, SparseVector ratings,
                                        SparseVector predictions) {
            for (Long2DoubleMap.Entry e: ratings.fast()) {
                double pv = predictions.get(e.getLongKey());
                npreds += 1;
                if (!Double.isNaN(pv))
                    ngood += 1;
            }
            nusers += 1;
        }

        @Override
        public String[] results() {
            double coverage = (double) ngood / npreds;
            logger.info("Coverage: {}", coverage);
            
            return new String[]{
                    Integer.toString(nusers), 
                    Integer.toString(npreds),
                    Integer.toString(ngood), 
                    Double.toString(coverage) 
            };
        }

    }
}
