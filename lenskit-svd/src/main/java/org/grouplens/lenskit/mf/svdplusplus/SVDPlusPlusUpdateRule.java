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
package org.grouplens.lenskit.mf.svdplusplus;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.grouplens.lenskit.mf.funksvd.TrainingEstimator;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.Math;

/**
 * Configuration for computing SVDPlusPlus updates.
 *
 * @since 2.0
 */
@Shareable
public final class SVDPlusPlusUpdateRule implements Serializable {
    private static final long serialVersionUID = 2L;

    private final double learningRate;
    private final double trainingRegularization;
    private final ItemScorer baseline;
    private final ClampingFunction clampingFunction;
    private final StoppingCondition stoppingCondition;

    /**
     * Construct a new SVDPlusPlus configuration.
     *
     * @param lrate The learning rate.
     * @param reg   The regularization term.
     * @param clamp The clamping function.
     * @param stop  The stopping condition
     */
    @Inject
    public SVDPlusPlusUpdateRule(@LearningRate double lrate,
                             @RegularizationTerm double reg,
                             @BaselineScorer ItemScorer bl,
                             ClampingFunction clamp,
                             StoppingCondition stop) {
        learningRate = lrate;
        trainingRegularization = reg;
        baseline = bl;
        clampingFunction = clamp;
        stoppingCondition = stop;
    }

    /**
     * Create an estimator to use while training the recommender.
     *
     * @return The estimator to use.
     */
    public TrainingEstimator makeEstimator(PreferenceSnapshot snapshot) {
        return new TrainingEstimator(snapshot, baseline, clampingFunction);
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

    public TrainingLoopController getTrainingLoopController() {
        return stoppingCondition.newLoop();
    }

    /**
     * Compute the user feature added up with normalized item implicit features.
     * @param ufv The user feature value.
     * @param userRatings The ratings of the user.
     * @return The user feature value added up with normalized item feature values.
     */
    public double computeUserAddedFeature(double uf, double[] iifv, 
                                          FastCollection<IndexedPreference> userRatings) {
        double uside = uf;
        int ratnum = userRatings.size();
        for (IndexedPreference r : CollectionUtils.fast(userRatings)) {
            int ratedidx = r.getItemIndex();
            uside += iifv[ratedidx] / Math.sqrt((double)ratnum);
        }
        return uside;
    }

    /**
     * Compute the error in the current estimate of a rating.
     * @param uid The user ID.
     * @param iid The item ID.
     * @param uside The user feature value added up with normalized item feature values.
     * @param ifv The item feature value.
     * @param estimate The estimate through the previous feature.
     * @param rating The rating value.
     * @return The error in predicting the rating.
     */
    public double computeError(long uid, long iid, double uside, double ifv,
                               double rating, double estimate) {
        // Compute prediction
        double pred = estimate;
        pred += uside * ifv;

        // Clamp the prediction first
        pred = clampingFunction.apply(uid, iid, pred);

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
     * @param ifv The item feature value.
     * @param uside The user feature value added up with normalized item feature values.
     * @return The adjustment to be made to the item feature value.
     */
    public double itemUpdate(double err, double ifv, double uside) {
        double delta = err * uside - trainingRegularization * ifv;
        return delta * learningRate;
    }

    /**
     * Compute the update for an item implicit feature value from error & feature values.
     * @param err The error.
     * @param ifv The item feature value.
     * @param iifv The item implicit feature value.
     * @param ratnum The number of the user's ratings.
     * @return The adjustment to be made to the item feature value.
     */
    public double itemImpUpdate(double err, double ifv, double iifv, int ratnum) {
        double delta = err * ifv / Math.sqrt((double)ratnum) - trainingRegularization * iifv;
        return delta * learningRate;
    }
}
