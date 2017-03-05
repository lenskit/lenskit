package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.lenskit.slim.LinearRegressionHelper.addVectors;


/**
 * Created by tmc on 2/10/17.
 * minimize 1/2*||a_j - A*w_j||^2 + beta/2*||wj||^2 + lambda*||w_j||
 */

public abstract class LinearRegressionAbstract {
    protected static final Logger logger = LoggerFactory.getLogger(LinearRegressionAbstract.class);
    protected final double beta;
    protected final double lambda;
    protected final boolean intercept;
    protected final int iterNum;

    /**
     * constructor of linear regression
     * @param beta coefficient of L2 term
     * @param lambda coefficient of L1 term
     * @param intercept whether or not including intercept
     * @param iterNum iteration number
     */
    public LinearRegressionAbstract(double beta, double lambda, boolean intercept, int iterNum) {
        this.beta = beta;
        this.lambda = lambda;
        this.intercept = intercept;
        this.iterNum = iterNum;
    }

    /**
     * compute prediction of single observation
     * @param observation test data
     * @param weight weight
     * @return prediction
     */
    public double computePrediction(Long2DoubleMap observation, Long2DoubleMap weight) {
        return Vectors.dotProduct(weight, observation);
    }

    /**
     * compute residual vector
     * @param labels label vector
     * @param dataMatrix data matrix
     * @param weights weights vector
     * @return residual vector
     */
    public Long2DoubleMap computeResiduals(Long2DoubleMap labels, Map<Long, Long2DoubleMap> dataMatrix, Long2DoubleMap weights) {
        Long2DoubleMap predictions = predict(dataMatrix, weights);
        return addVectors(labels, Vectors.multiplyScalar(predictions,-1.0));
    }

    /**
     * learning process
     * @param labels label vector
     * @param trainingDataMatrix observations matrix row: user ratings for different items, column: item ratings of different users
     * @return weight vector
     */
    public abstract Long2DoubleMap fit(Long2DoubleMap labels, Map<Long, Long2DoubleMap> trainingDataMatrix);


    /**
     * optimized computation to update residual after one element of weight vector changed
     * @param r original residual vector
     * @param column one column of training matrix whose index equal to index of weight that is just updated
     * @param weightToUpdate one element of weight vector before update
     * @param weightUpdated one element of weight vector after update
     * @return
     */
    public Long2DoubleMap updateResiduals(Long2DoubleMap r, Long2DoubleMap column, double weightToUpdate, double weightUpdated) {
        Long2DoubleMap residualsIncrement = Vectors.multiplyScalar(column, weightToUpdate - weightUpdated);
        return addVectors(r, residualsIncrement);
    }

    /**
     * compute predictions given test data matrix and weights
     * @param testData data matrix
     * @param weights weight
     * @return prediction vector
     */
    public Long2DoubleMap predict(Map<Long, Long2DoubleMap> testData, Long2DoubleMap weights) {
        Long2DoubleMap predictions = new Long2DoubleOpenHashMap();
        for (Map.Entry<Long, Long2DoubleMap> row : testData.entrySet()) {
            long key = row.getKey();
            Long2DoubleMap value = row.getValue();
            double predictValue = computePrediction(value, weights);
            predictions.put(key, predictValue);
        }
        return predictions;
    }

    /**
     * Compute target function of elastic regression
     * @param residuals
     * @param weights
     * @return
     */
    public double computeLossFunction(Long2DoubleMap residuals, Long2DoubleMap weights) {
        return 1/2.0 * Vectors.sumOfSquares(residuals) + beta/2* Vectors.sumOfSquares(weights) + lambda* Vectors.sumAbs(weights);
    }

}
