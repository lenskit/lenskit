package org.grouplens.lenskit.eval.predict;

import static java.lang.Math.abs;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MAEEvaluator implements PredictionEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(MAEEvaluator.class);
    
    @Override
    public PredictionEvaluationAccumulator makeAccumulator() {
        return new Accum();
    }

    @Override
    public String getName() {
        return "MAE";
    }
    
    static class Accum implements PredictionEvaluationAccumulator {
        private double err = 0;
        private int n = 0;
        
        @Override
        public void evaluatePrediction(SparseVector ratings,
                SparseVector predictions) {
            for (Long2DoubleMap.Entry e: predictions.fast()) {
                if (Double.isNaN(e.getDoubleValue())) continue;
                
                err += abs(e.getDoubleValue() - ratings.get(e.getLongKey()));
                n++;
            }
        }

        @Override
        public double finish() {
            double v = err / n;
            logger.info("MAE: {}", v);
            return v;
        }
        
    }
}
