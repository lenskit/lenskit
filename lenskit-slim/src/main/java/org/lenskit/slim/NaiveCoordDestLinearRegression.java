package org.lenskit.slim;


import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import org.lenskit.util.math.Vectors;

import java.util.Map;

import static org.lenskit.slim.LinearRegressionHelper.*;


/**
 * Created by tmc on 2/14/17.
 */
public class NaiveCoordDestLinearRegression extends LinearRegressionAbstract {

    public NaiveCoordDestLinearRegression(double beta, double lambda, boolean intercept, int iterNum) {
        super(beta, lambda, intercept, iterNum);
    }

    public double updateWeight(Long2DoubleMap column, double weightToUpdate, Long2DoubleMap residuals, double lambda, double beta) {
        double norm2column = Vectors.dotProduct(column, column);
        double firstTerm = Vectors.dotProduct(column, residuals) + norm2column*weightToUpdate;
        double weightUpdated = softThresholding(firstTerm, lambda) / (norm2column + beta);
        return weightUpdated;
    }

    /**
     * Training process of SLIM using naive coordinate descent update
     * @param labels label vector
     * @param trainingDataMatrix observations matrix row: user ratings for different items, column: item ratings of different users
     * @return weight vectors learned
     */
    @Override
    public Long2DoubleMap fit(Long2DoubleMap labels, Map<Long, Long2DoubleMap> trainingDataMatrix) {
        Long2DoubleMap weights = new Long2DoubleOpenHashMap();
        Map<Long, Long2DoubleMap> trainingDataColumnWise = transposeMap(trainingDataMatrix);
        LongOpenHashBigSet itemSet = new LongOpenHashBigSet(trainingDataColumnWise.keySet());

        for (long i : itemSet) { weights.put(i, 0.0); }
        Long2DoubleMap residuals = computeResiduals(labels, trainingDataMatrix, weights);
        double[] loss = new double[2];
        double lossDiff = Double.POSITIVE_INFINITY;
        for (int k = 0; k < iterNum && lossDiff > 0.005; k++) {
            for (long j : itemSet) {
                Long2DoubleMap column = trainingDataColumnWise.get(j);
                double weightToUpdate = weights.get(j);
                double weightUpdated = updateWeight(column, weightToUpdate, residuals, lambda, beta);
                weights.put(j, weightUpdated);
                residuals = updateResiduals(residuals, column, weightToUpdate, weightUpdated);
                loss[k%2] = computeLossFunction(residuals, weights);
                logger.info("loss function reduced to {}", loss[0]);
                lossDiff = Math.abs(loss[0] - loss[1]);
            }
        }
        return weights;
    }
}
