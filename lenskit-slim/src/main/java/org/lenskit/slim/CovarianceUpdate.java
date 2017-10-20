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

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Vectors;

import javax.inject.Inject;

import java.io.Serializable;

import static org.lenskit.slim.LinearRegressionHelper.*;

/**
 * Covariance Update Rule of Coordinate Descent
 * Implementation of Paper: Regularization Paths for Generalized Linear Models via Coordinate Descent
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public final class CovarianceUpdate extends SLIMScoringStrategy implements Serializable {
    private static final long serialVersionUID = 4L;

    private final double epsilon;

    @Inject
    public CovarianceUpdate(SLIMUpdateParameters parameters,
                            @MinItemWeight Double eps) {
        super(parameters);
        epsilon = eps;
    }

    /**
     * Covariance update for one element of weight vector in a circle of coordinate descent
     * Slightly different from formula (5), (8) and (9) in order to support non-normalized rating vectors
     *
     * @param itemXj a vector of training matrix whose index equal to the one of weights that needs to be updated
     * @param dotProdOfXjY first term of formula 9
     * @param dotProdsXjXks dot product vector whose element k is inner-product <x_j, x_k>
     * @param nonzeroWeights weight vector with nonzero value
     * @param lambda L1-norm term
     * @param beta L2-norm term
     * @return the updated element of weight vector
     */
    public double updateWeight(Long2DoubleMap itemXj, double dotProdOfXjY, Long2DoubleMap dotProdsXjXks, Long2DoubleMap nonzeroWeights, double lambda, double beta) {
        double norm2OfXj = Vectors.dotProduct(itemXj, itemXj);
        double firstTerm = dotProdOfXjY - Vectors.dotProduct(dotProdsXjXks, nonzeroWeights);
        return softThresholding(firstTerm, lambda) / (norm2OfXj + beta);
    }


    /**
     * Training process of SLIM using covariance update of coordinate descent
     * Get a learned weight using covariance update (formula (9))
     * Initialize weight to zero as in this case the L1-term and L2-term become zero in loss function,
     * which means final learned weight should at least make the value of loss function less than the starting value.
     *
     * @param labels label vector which is one column of rating matrix (Map of all item IDs to item rating vectors)
     * @param trainingDataMatrix Map of Item IDs to item rating vectors.
     * @param covMatrix Map of Item IDs to item-item inner-products
     * @param itemYId item ID of label vector {@code labels}
     * @return weight vector learned
     */
    @Override
    public Long2DoubleMap fit(Long2DoubleMap labels, Long2ObjectMap<Long2DoubleMap> trainingDataMatrix, Long2ObjectMap<Long2DoubleMap> covMatrix, long itemYId) {
        // initialize training weights to be empty
        Long2DoubleMap weights = new Long2DoubleOpenHashMap();

        final double lambda = updateParameters.getLambda();
        final double beta = updateParameters.getBeta();

        Long2DoubleMap residuals = computeResiduals(labels, trainingDataMatrix, weights);
        TrainingLoopController controller = updateParameters.getTrainingLoopController();

        double lossValue = Double.POSITIVE_INFINITY;
//        double lossValue = computeLossFunction(residuals, weights);
//        logger.debug("train item {}: initial value of loss function is {} \n",itemYId, lossValue);

        while (controller.keepTraining(lossValue)) {
            LongIterator items = trainingDataMatrix.keySet().iterator();
            // one round of coordinate descent update
            while (items.hasNext()) {
                long j = items.nextLong();
                Long2DoubleMap itemXj = trainingDataMatrix.get(j);
                Long2DoubleMap dotProdsOfXjXks = covMatrix.get(j);
                if (dotProdsOfXjXks == null) { dotProdsOfXjXks = new Long2DoubleOpenHashMap(); }
                double dotProdOfXjY = dotProdsOfXjXks.get(itemYId);

                Long2DoubleMap nonzeroWeights = Vectors.filterValues(weights, 0.0, epsilon);
                nonzeroWeights.remove(j);

                double weightToUpdate = weights.get(j);
                double weightUpdated = updateWeight(itemXj, dotProdOfXjY, dotProdsOfXjXks, nonzeroWeights, lambda, beta);
                weights.put(j, weightUpdated);
                residuals = updateResiduals(residuals, itemXj, weightToUpdate, weightUpdated);
            }

            lossValue = computeLossFunction(residuals, weights);
            int iterationCount = controller.getIterationCount();
            logger.debug("train item {}: {}th round iteration and loss function reduced to {} \n",itemYId, iterationCount, lossValue);
//            System.out.println("train item " + itemYId + ": " + iterationCount + "th round iteration and loss function reduced to " + lossValue);
        }
        return LongUtils.frozenMap(weights);
    }

}
