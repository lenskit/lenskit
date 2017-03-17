/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static org.lenskit.slim.LinearRegressionHelper.addVectors;


/**
 * Linear regression which minimize following loss function
 * 1/2*||a_j - A*w_j||^2 + beta/2*||wj||^2 + lambda*||w_j||
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(CovarianceUpdate.class)
public abstract class SLIMScoringStrategy {
    protected static final Logger logger = LoggerFactory.getLogger(SLIMScoringStrategy.class);
    protected final SLIMUpdateParameters updateParameters;

    @Inject
    public SLIMScoringStrategy(SLIMUpdateParameters updateParameters) {
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
     *
     * @param labels label vector
     * @param dataMatrix column-wise data matrix
     * @param weights weights vector
     * @return residual vector
     */
    public Long2DoubleMap computeResiduals(Long2DoubleMap labels, Long2ObjectMap<Long2DoubleMap> dataMatrix, Long2DoubleMap weights) {
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
    public abstract Long2DoubleMap fit(Long2DoubleMap labels, Long2ObjectMap<Long2DoubleMap> trainingDataMatrix);

    /**
     * learning process passed in pre-computed inner-products to speed up learning iterations
     *
     * @param labels label vector
     * @param trainingDataMatrix Map of Item IDs to item rating vectors.
     * @param covM Map of Item IDs to item-item inner-products vectors
     * @param item item ID of label vector {@code labels}
     * @return a trained weight vector
     */
    public abstract Long2DoubleMap fit(Long2DoubleMap labels, Long2ObjectMap<Long2DoubleMap> trainingDataMatrix, Long2ObjectMap<Long2DoubleMap> covM, long item);


    /**
     * optimized computation of updating residual after one element of weight vector changed
     *
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
     *
     * @param testData column-wise data matrix
     * @param weights weight
     * @return prediction vector
     */
    public Long2DoubleMap predict(Long2ObjectMap<Long2DoubleMap> testData, Long2DoubleMap weights) {
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
     *
     * @param residuals residual vector of linear regression
     * @param weights learned weight
     * @return value of loss function
     */
    public double computeLossFunction(Long2DoubleMap residuals, Long2DoubleMap weights) {
        double beta = updateParameters.getBeta();
        double lambda = updateParameters.getLambda();
        return 1/2.0 * Vectors.sumOfSquares(residuals) + beta/2* Vectors.sumOfSquares(weights) + lambda* Vectors.sumAbs(weights);
    }

}
