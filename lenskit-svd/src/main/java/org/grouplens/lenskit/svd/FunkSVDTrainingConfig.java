/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.svd;

import javax.inject.Inject;

import org.grouplens.lenskit.svd.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.svd.params.RegularizationTerm;
import org.grouplens.lenskit.svd.params.TrainingThreshold;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;

/**
 * Configuration for computing FunkSVD updates. The config produces
 * {@link FunkSVDFeatureTrainer}s to train individual features.
 *
 * @since 1.0
 */
public class FunkSVDTrainingConfig {

    private final int iterationCount;
    private final double learningRate;
    private final double trainingThreshold;
    private final double trainingRegularization;
    private final ClampingFunction clampingFunction;

    /**
     * Construct a new FunkSVD configuration.
     *
     * @param lrate The learning rate.
     * @param threshold The training threshold (stop after delta RMSE drops below this).
     * @param reg The regularization term.
     * @param clamp The clamping function.
     * @param iterCount The max iteration count.
     */
    @Inject
    public FunkSVDTrainingConfig(@LearningRate double lrate,
                                 @TrainingThreshold double threshold,
                                 @RegularizationTerm double reg,
                                 ClampingFunction clamp,
                                 @IterationCount int iterCount) {
        learningRate = lrate;
        trainingThreshold = threshold;
        trainingRegularization = reg;
        clampingFunction = clamp;
        iterationCount = iterCount;
    }

    /**
     * Create a new trainer using this configuration.
     *
     * @return A new feature trainer for iteratively training a single
     *         FunkSVD feature.
     */
    public FunkSVDFeatureTrainer newTrainer() {
        return new FunkSVDFeatureTrainer(learningRate, trainingThreshold,
                                         trainingRegularization, clampingFunction, iterationCount);
    }

    public double getIterationCount() {
        return iterationCount;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getTrainingThreshold() {
        return trainingThreshold;
    }

    public double getTrainingRegularization() {
        return trainingRegularization;
    }

    public ClampingFunction getClampingFunction() {
        return clampingFunction;
    }
}
