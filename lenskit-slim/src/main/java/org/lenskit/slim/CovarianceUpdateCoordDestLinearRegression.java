package org.lenskit.slim;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Vectors;

import javax.annotation.Nonnull;
import java.util.Map;

import static org.lenskit.slim.LinearRegressionHelper.*;



public class CovarianceUpdateCoordDestLinearRegression extends LinearRegressionAbstract {

    public CovarianceUpdateCoordDestLinearRegression(SLIMUpdateParameters parameters) {
        super(parameters);
    }

    /**
     * Covariance UpdateDescentRule of Coordinate Descent
     * Paper: Regularization Paths for Generalized Linear Models via Coordinate Descent
     * Formula (5) and (9)

     * @param column column vector of training matrix whose index equal to the one of weights that needs to be updated
     * @param dotProductOfXY first term of formula 9
     * @param vectorOfDotProductOfXjXks dot product vector whose element k is <x_j, x_k>
     * @param nonzeroWeights weights vector with nonzero value
     * @param lambda
     * @param beta
     * @return
     */
    public double updateWeight(Long2DoubleMap column, double dotProductOfXY, Long2DoubleMap vectorOfDotProductOfXjXks, Long2DoubleMap nonzeroWeights, double lambda, double beta) {
        double norm2column = Vectors.dotProduct(column, column);
        double firstTerm = dotProductOfXY - Vectors.dotProduct(vectorOfDotProductOfXjXks, nonzeroWeights);
        double weightUpdated = softThresholding(firstTerm, lambda) / (norm2column + beta);
        return weightUpdated;
    }

    @Override
    public Long2DoubleMap fit(Long2DoubleMap labels, Map<Long, Long2DoubleMap> trainingDataMatrix) {
        Long2DoubleMap weights = new Long2DoubleOpenHashMap();
        LongOpenHashBigSet itemSet = new LongOpenHashBigSet(trainingDataMatrix.keySet());
        final double lambda = updateParameters.getLambda();
        final double beta = updateParameters.getBeta();
        //for (long i : itemSet) { weights.put(i, 0.0); }

        Long2DoubleMap dotProdOfXY = new Long2DoubleOpenHashMap();
        Map<Long, Long2DoubleMap> correlationOfColumns = Maps.newHashMap();
        Long2DoubleMap residuals = computeResiduals(labels, trainingDataMatrix, weights);

        double[] loss = new double[2];
        double lossDiff = Double.POSITIVE_INFINITY;
        TrainingLoopController controller = updateParameters.getTrainingLoopController();
        int k = 0;
        while (controller.keepTraining(lossDiff)) {
            for (long j : itemSet) {
                Long2DoubleMap column = trainingDataMatrix.get(j);
                double dotProdOfXjY;

                // Storing dot product of column j and labels
                if (dotProdOfXY.containsKey(j)) {
                    dotProdOfXjY = dotProdOfXY.get(j);
                } else {
                    dotProdOfXjY = Vectors.dotProduct(column, labels);
                    dotProdOfXY.put(j, dotProdOfXjY);
                }

                //Long2DoubleMap weightsTemp = new Long2DoubleOpenHashMap(weights);
                Long2DoubleMap nonzeroWeights = filterValues(weights, 0.0);

                // Storing dot product of column j and column k(s) into correlationOfColumns row j
                if (correlationOfColumns.containsKey(j)) {
                    Long2DoubleMap tempRowOfCorCol = correlationOfColumns.get(j);
                    for (long weightsId : nonzeroWeights.keySet()) {
                        if (!tempRowOfCorCol.containsKey(weightsId)) {
                            double prod = Vectors.dotProduct(column, trainingDataMatrix.get(weightsId));
                            correlationOfColumns.get(j).put(weightsId, prod);
                        }
                    }
                } else {
                    Long2DoubleMap rowj = new Long2DoubleOpenHashMap();
                    for (long weightsId : nonzeroWeights.keySet()) {
                        double prod = Vectors.dotProduct(column, trainingDataMatrix.get(weightsId));
                        rowj.put(weightsId, prod);
                    }
                    correlationOfColumns.put(j, rowj);
                }

                //Long2DoubleMap dotProdsOfXjXk = new Long2DoubleOpenHashMap(correlationOfColumns.get(j));
                Long2DoubleMap dotProdsOfXjXk = correlationOfColumns.get(j);
                nonzeroWeights.remove(j);

                double weightToUpdate = weights.get(j);
                double weightUpdated = updateWeight(column, dotProdOfXjY, dotProdsOfXjXk, nonzeroWeights, lambda, beta);
                weights.put(j, weightUpdated);
                residuals = updateResiduals(residuals, column, weightToUpdate, weightUpdated);
                //residuals = computeResiduals(labels, trainingDataMatrix, weights);
                loss[k%2] = computeLossFunction(residuals, weights);
                logger.info("{}th iteration and loss function reduced to {} and {} \n and weights is {}", k, loss[0], loss[1], weights);
                lossDiff = Math.abs(loss[0] - loss[1]);

            }
            k++;
        }
        return LongUtils.frozenMap(weights);
    }

    /**
     * Given pre-computed inner-products matrix
     * @param labels
     * @param trainingDataMatrix
     * @param trainingDataColumnWise
     * @param covM
     * @param item
     * @return
     */
    public Long2DoubleMap fit(Long2DoubleMap labels, Map<Long, Long2DoubleMap> trainingDataMatrix, Map<Long, Long2DoubleMap> trainingDataColumnWise, Map<Long, Long2DoubleMap> covM, long item) {
        Long2DoubleMap weights = new Long2DoubleOpenHashMap();
        LongOpenHashBigSet itemSet = new LongOpenHashBigSet(trainingDataColumnWise.keySet());
        final double lambda = updateParameters.getLambda();
        final double beta = updateParameters.getBeta();
        for (long i : itemSet) { weights.put(i, 0.0); }

        Long2DoubleMap residuals = computeResiduals(labels, trainingDataMatrix, weights);
        TrainingLoopController controller = updateParameters.getTrainingLoopController();
        double[] loss = new double[2];
        double lossDiff = Double.POSITIVE_INFINITY;

        int k = 0;
        while (controller.keepTraining(lossDiff)) {
            for (long j : itemSet) {
                Long2DoubleMap column = trainingDataColumnWise.get(j);
                double dotProdOfXjY;
                Long2DoubleMap covColumn = covM.get(item);
                //dotProdOfXjY = covM.getOrDefault(item, new Long2DoubleOpenHashMap()).getOrDefault(j, 0.0);

                if (covColumn == null) { covColumn = new Long2DoubleOpenHashMap(); }
                dotProdOfXjY = covColumn.get(j);
                Long2DoubleMap nonzeroWeights = filterValues(weights, 0.0);

                Long2DoubleMap dotProdsOfXjXk = covM.get(j);
                nonzeroWeights.remove(j);

                double weightToUpdate = weights.get(j);
                double weightUpdated = updateWeight(column, dotProdOfXjY, dotProdsOfXjXk, nonzeroWeights, lambda, beta);
                weights.put(j, weightUpdated);
                residuals = updateResiduals(residuals, column, weightToUpdate, weightUpdated);
                loss[k%2] = computeLossFunction(residuals, weights);
                //logger.info("loss function reduced to {} and weights is {}", loss[1], weights);
                lossDiff = Math.abs(loss[0] - loss[1]);
                k++;
            }
        }
        return weights;
    }


}
