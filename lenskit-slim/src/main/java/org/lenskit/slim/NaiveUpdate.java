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

import static org.lenskit.slim.LinearRegressionHelper.*;


/**
 * Naive Update Rule of Coordinate Descent
 * Implementation of Paper: Regularization Paths for Generalized Linear Models via Coordinate Descent
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public final class NaiveUpdate extends SLIMScoringStrategy {

    @Inject
    public NaiveUpdate(SLIMUpdateParameters parameters) {
        super(parameters);
    }

    /**
     * Naive update for one element of weight vector in a circle of coordinate descent
     * Slight different from formula (7) (8) in order to support non-unit-normalized rating vector
     *
     * @param colXj column of Xj
     * @param weightToUpdate an element of weight vector needs to update
     * @param residuals current residual vector
     * @param lambda L1-Norm Term
     * @param beta L2-Norm Term
     * @return the updated element of weight vector
     */
    private double updateWeight(Long2DoubleMap colXj, double weightToUpdate, Long2DoubleMap residuals, double lambda, double beta) {
        double norm2OfXj = Vectors.dotProduct(colXj, colXj);
        double firstTerm = Vectors.dotProduct(colXj, residuals) + norm2OfXj*weightToUpdate;
        return softThresholding(firstTerm, lambda) / (norm2OfXj + beta);
    }


    /**
     * Training process of SLIM using naive coordinate descent update
     *
     * @param labels The label vector
     * @param trainingDataMatrix Map of item IDs to item rating vectors.
     * @return weight vector learned
     */

    @Override
    public Long2DoubleMap fit(Long2DoubleMap labels, Long2ObjectMap<Long2DoubleMap> trainingDataMatrix, Long2ObjectMap<Long2DoubleMap> covM, long itemYId) {
        Long2DoubleMap weights = new Long2DoubleOpenHashMap();

        final double lambda = updateParameters.getLambda();
        final double beta = updateParameters.getBeta();

        Long2DoubleMap residuals = computeResiduals(labels, trainingDataMatrix, weights);
        double lossValue = Double.POSITIVE_INFINITY;
        TrainingLoopController controller = updateParameters.getTrainingLoopController();

        while (controller.keepTraining(lossValue)) {
            LongIterator items = trainingDataMatrix.keySet().iterator();
            while (items.hasNext()) {
                long j = items.nextLong();
                Long2DoubleMap column = trainingDataMatrix.get(j);
                double weightToUpdate = weights.get(j);
                double weightUpdated = updateWeight(column, weightToUpdate, residuals, lambda, beta);
                weights.put(j, weightUpdated);
                residuals = updateResiduals(residuals, column, weightToUpdate, weightUpdated);
            }
            lossValue = computeLossFunction(residuals, weights);
            int iterationCount = controller.getIterationCount();
            logger.debug("train item {}: {}th round iteration and loss function reduced to {} \n",itemYId, iterationCount, lossValue);
        }
        return LongUtils.frozenMap(weights);
    }

}
