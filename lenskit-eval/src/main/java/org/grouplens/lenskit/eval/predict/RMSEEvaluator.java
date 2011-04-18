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
