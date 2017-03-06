package org.lenskit.slim;


import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.util.math.Vectors;

import java.util.Map;

import static org.lenskit.slim.LinearRegressionHelper.*;


/**
 * Created by tmc on 2/14/17.
 */
public class NaiveCoordDestLinearRegression extends LinearRegressionAbstract {

    public NaiveCoordDestLinearRegression(SLIMUpdateParameters parameters) {
        super(parameters);
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
        LongOpenHashBigSet itemSet = new LongOpenHashBigSet(trainingDataMatrix.keySet());
        final double lambda = updateParameters.getLambda();
        final double beta = updateParameters.getBeta();

        //for (long i : itemSet) { weights.put(i, 0.0); }
        Long2DoubleMap residuals = computeResiduals(labels, trainingDataMatrix, weights);
        double[] loss = new double[2];
        double lossDiff = Double.POSITIVE_INFINITY;
        TrainingLoopController controller = updateParameters.getTrainingLoopController();
        int k = 0;
        while (controller.keepTraining(lossDiff)) {
            for (long j : itemSet) {
                Long2DoubleMap column = trainingDataMatrix.get(j);
                double weightToUpdate = weights.get(j);
                double weightUpdated = updateWeight(column, weightToUpdate, residuals, lambda, beta);
                weights.put(j, weightUpdated);
                residuals = updateResiduals(residuals, column, weightToUpdate, weightUpdated);
                loss[k%2] = computeLossFunction(residuals, weights);
                //logger.info("loss function reduced to {}", loss[0]);
                lossDiff = Math.abs(loss[0] - loss[1]);
                k++;
            }
        }
        return weights;
    }
}
