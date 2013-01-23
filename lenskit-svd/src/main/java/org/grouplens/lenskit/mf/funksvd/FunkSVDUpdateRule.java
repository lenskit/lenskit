/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.mf.funksvd;

import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.grouplens.lenskit.iterative.params.LearningRate;
import org.grouplens.lenskit.iterative.params.RegularizationTerm;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Configuration for computing FunkSVD updates.
 *
 * @since 1.0
 */
@Shareable
public final class FunkSVDUpdateRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double learningRate;
    private final double trainingRegularization;
    private final ClampingFunction clampingFunction;
    private final TrainingLoopController trainingLoopController;

    /**
     * Construct a new FunkSVD configuration.
     *
     * @param lrate The learning rate.
     * @param reg   The regularization term.
     * @param clamp The clamping function.
     * @param controller
     */
    @Inject
    public FunkSVDUpdateRule(@LearningRate double lrate,
                             @RegularizationTerm double reg,
                             ClampingFunction clamp,
                             TrainingLoopController controller) {
        learningRate = lrate;
        trainingRegularization = reg;
        clampingFunction = clamp;
        trainingLoopController = controller;
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

    public TrainingLoopController getTrainingLoopController() {
        return trainingLoopController;
    }

    /**
     * Compute the error in the current estimate of a rating.
     * @param uid The user ID.
     * @param iid The item ID.
     * @param trail The trailing value (contribution of remaining features).
     * @param estimate The estimate through the previous feature.
     * @param rating The rating value.
     * @param ufv The user feature value.
     * @param ifv The item feature value.
     * @return The error in predicting the rating.
     */
    public double computeError(long uid, long iid, double trail,
                               double estimate, double rating,
                               double ufv, double ifv) {
        // Compute prediction
        double pred = estimate + ufv * ifv;

        // Clamp the prediction first
        pred = clampingFunction.apply(uid, iid, pred);

        // Add the trailing value, then clamp the result again
        pred = clampingFunction.apply(uid, iid, pred + trail);

        // Compute the err and store this value
        return rating - pred;
    }

    /**
     * Compute the update for a user feature value from error & feature values.
     * @param err The error.
     * @param ufv The user feature value.
     * @param ifv The item feature value.
     * @return The adjustment to be made to the user feature value.
     */
    public double userUpdate(double err, double ufv, double ifv) {
        double delta = err * ifv - trainingRegularization * ufv;
        return delta * learningRate;
    }

    /**
     * Compute the update for an item feature value from error & feature values.
     * @param err The error.
     * @param ufv The user feature value.
     * @param ifv The item feature value.
     * @return The adjustment to be made to the item feature value.
     */
    public double itemUpdate(double err, double ufv, double ifv) {
        double delta = err * ufv - trainingRegularization * ifv;
        return delta * learningRate;
    }
}
