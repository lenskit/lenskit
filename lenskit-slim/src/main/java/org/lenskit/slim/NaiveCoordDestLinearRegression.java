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
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Vectors;

import javax.inject.Inject;
import java.util.Map;

import static org.lenskit.slim.LinearRegressionHelper.*;


/**
 * Created by tmc on 2/14/17.
 */
public class NaiveCoordDestLinearRegression extends AbstractLinearRegression {

    @Inject
    public NaiveCoordDestLinearRegression(SlimUpdateParameters parameters) {
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
     * @param trainingDataMatrix Map of item IDs to item rating vectors.
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
            }
            k++;
        }
        return LongUtils.frozenMap(weights);
    }

    @Override
    public Long2DoubleMap fit(Long2DoubleMap labels, Map<Long, Long2DoubleMap> trainingDataMatrix, Map<Long, Long2DoubleMap> covM, long item) {
        return fit(labels, trainingDataMatrix);
    }

}
