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

import org.grouplens.lenskit.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.svd.params.RegularizationTerm;
import org.grouplens.lenskit.svd.params.TrainingThreshold;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.util.iterative.StoppingCondition;

/**
 * Configuration for computing FunkSVD updates. The config produces
 * {@link FunkSVDFeatureTrainer}s to train individual features.
 *
 * @since 1.0
 */
public class FunkSVDTrainingConfig {

    private final double learningRate;
    private final double trainingRegularization;
    private final ClampingFunction clampingFunction;
    private StoppingCondition stoppingCondition;

    /**
     * Construct a new FunkSVD configuration.
     *
     * @param lrate The learning rate.
     * @param reg The regularization term.
     * @param clamp The clamping function.
     * @param stop The stopping condition.
     */
    @Inject
    public FunkSVDTrainingConfig(@LearningRate double lrate,
                                 @RegularizationTerm double reg,
                                 ClampingFunction clamp,
                                 StoppingCondition stop) {
        learningRate = lrate;
        trainingRegularization = reg;
        clampingFunction = clamp;
        stoppingCondition = stop;
    }

    /**
     * Create a new trainer using this configuration.
     *
     * @return A new feature trainer for iteratively training a single
     *         FunkSVD feature.
     */
    public FunkSVDFeatureTrainer newTrainer() {
        return new FunkSVDFeatureTrainer(this);
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getTrainingRegularization() {
        return trainingRegularization;
    }

    public ClampingFunction getClampingFunction() {
        return clampingFunction;
    }

    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }
}
