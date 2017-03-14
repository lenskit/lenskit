package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static org.lenskit.slim.LinearRegressionHelper.addVectors;


/**
 * Created by tmc on 2/10/17.
 * minimize 1/2*||a_j - A*w_j||^2 + beta/2*||wj||^2 + lambda*||w_j||
 */
@LinearRegression
@DefaultImplementation(CovarianceUpdateCoordDestLinearRegression.class)
public abstract class LinearRegressionAbstract {
    protected static final Logger logger = LoggerFactory.getLogger(LinearRegressionAbstract.class);
    protected final SlimUpdateParameters updateParameters;

    @Inject
    public LinearRegressionAbstract(SlimUpdateParameters updateParameters) {
        this.updateParameters = updateParameters;
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
     *
     * @param labels label vector
     * @param trainingDataMatrix observations matrix row: user ratings for different items, column: item ratings of different users
     * @return a trained weight vector
     */
    public abstract Long2DoubleMap fit(Long2DoubleMap labels, Map<Long, Long2DoubleMap> trainingDataMatrix);

    /**
     * learning process passed in pre-computed inner-products to speed up learning iterations
     *
     * @param labels label vector
     * @param trainingDataMatrix Map of Item IDs to item rating vectors.
     * @param covM Map of Item IDs to item-item inner-products vectors
     * @param item item ID of label vector {@code labels}
     * @return a trained weight vector
     */
    public abstract Long2DoubleMap fit(Long2DoubleMap labels, Map<Long, Long2DoubleMap> trainingDataMatrix, Map<Long, Long2DoubleMap> covM, long item);


    /**
     * optimized computation to update residual after one element of weight vector changed
     * @param r original residual vector
     * @param column one column of training matrix whose index equal to index of weight that is just updated
     * @param weightToUpdate one element of weight vector before update
     * @param weightUpdated one element of weight vector after update
     * @return
     */
    public Long2DoubleMap updateResiduals(Long2DoubleMap r, Long2DoubleMap column, double weightToUpdate, double weightUpdated) {
        Long2DoubleMap residualsIncrement = Vectors.multiplyScalar(column, (weightToUpdate - weightUpdated));
        Long2DoubleMap residulsUpdated = addVectors(r, residualsIncrement);
        return residulsUpdated;
    }

    /**
     * compute predictions given test data matrix and learned weights
     * @param testData column-wise data matrix
     * @param weights weight
     * @return prediction vector
     */
    public Long2DoubleMap predict(Map<Long, Long2DoubleMap> testData, Long2DoubleMap weights) {
        Long2DoubleMap predictions = new Long2DoubleOpenHashMap();
        for (Map.Entry<Long, Long2DoubleMap> column : testData.entrySet()) {
            long key = column.getKey();
            Long2DoubleMap value = column.getValue();
            Long2DoubleMap vector = Vectors.multiplyScalar(value, weights.get(key));
            predictions = addVectors(predictions, vector);
        }
        return predictions;
    }

    /**
     * Compute loss function of elastic regression
     * @param residuals
     * @param weights
     * @return
     */
    public double computeLossFunction(Long2DoubleMap residuals, Long2DoubleMap weights) {
        double beta = updateParameters.getBeta();
        double lambda = updateParameters.getLambda();
        return 1/2.0 * Vectors.sumOfSquares(residuals) + beta/2* Vectors.sumOfSquares(weights) + lambda* Vectors.sumAbs(weights);
    }

}
