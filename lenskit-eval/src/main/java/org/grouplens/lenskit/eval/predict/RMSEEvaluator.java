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

import static java.lang.Math.sqrt;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMSEEvaluator implements PredictionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(RMSEEvaluator.class);

    @Override
    public PredictionEvaluationAccumulator makeAccumulator() {
        return new Accum();
    }

    @Override
    public String getName() {
        return "RMSE";
    }
    
    static class Accum implements PredictionEvaluationAccumulator {
        private double sse = 0;
        private int n = 0;
        
        @Override
        public void evaluatePrediction(SparseVector ratings,
                SparseVector predictions) {
            for (Long2DoubleMap.Entry e: predictions.fast()) {
                if (Double.isNaN(e.getDoubleValue())) continue;
                
                double err = e.getDoubleValue() - ratings.get(e.getLongKey());
                sse += err * err;
                n++;
            }
        }

        @Override
        public double finish() {
            double v = sqrt(sse / n);
            logger.info("RMSE: {}", v);
            return v;
        }
        
    }
}
