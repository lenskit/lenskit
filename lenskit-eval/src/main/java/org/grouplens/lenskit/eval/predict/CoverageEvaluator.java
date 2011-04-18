package org.grouplens.lenskit.eval.predict;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import org.grouplens.lenskit.data.vector.SparseVector;

public class CoverageEvaluator implements PredictionEvaluator {

    @Override
    public PredictionEvaluationAccumulator makeAccumulator() {
        return new Accum();
    }

    @Override
    public String getName() {
        return "Coverage";
    }
    
    static class Accum implements PredictionEvaluationAccumulator {
        private int npreds = 0;
        private int ngood = 0;
        
        @Override
        public void evaluatePrediction(SparseVector ratings,
                SparseVector predictions) {
            for (Long2DoubleMap.Entry e: ratings.fast()) {
                double pv = predictions.get(e.getLongKey());
                npreds += 1;
                if (!Double.isNaN(pv))
                    ngood += 1;
            }
        }

        @Override
        public double finish() {
            return (double) ngood / npreds;
        }
        
    }
}
