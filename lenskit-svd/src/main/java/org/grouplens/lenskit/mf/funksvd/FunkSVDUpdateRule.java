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

import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.vectors.MutableVec;
import org.grouplens.lenskit.vectors.Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Configuration for computing FunkSVD updates.
 *
 * @since 1.0
 */
@Shareable
public final class FunkSVDUpdateRule implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final Logger logger = LoggerFactory.getLogger(FunkSVDUpdateRule.class);

    private final double learningRate;
    private final double trainingRegularization;
    private final boolean useTrailingEstimate;
    private final BaselinePredictor baseline;
    private final ClampingFunction clampingFunction;
    private final StoppingCondition stoppingCondition;

    /**
     * Construct a new FunkSVD configuration.
     *
     * @param lrate The learning rate.
     * @param reg   The regularization term.
     * @param clamp The clamping function.
     * @param stop  The stopping condition
     */
    @Inject
    public FunkSVDUpdateRule(@LearningRate double lrate,
                             @RegularizationTerm double reg,
                             @UseTrailingEstimate boolean trail,
                             BaselinePredictor bl,
                             ClampingFunction clamp,
                             StoppingCondition stop) {
        learningRate = lrate;
        trainingRegularization = reg;
        baseline = bl;
        clampingFunction = clamp;
        stoppingCondition = stop;
        useTrailingEstimate = trail;
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

        if (useTrailingEstimate) {
            // Add the trailing value, then clamp the result again
            pred = clampingFunction.apply(uid, iid, pred + trail);
        }

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

    /**
     * Train a feature using a collection of ratings.
     *
     * @param estimates The current estimator.
     * @param ratings The rating collection.
     * @param ufvs The user feature values.
     * @param ifvs The item feature values.
     * @param trail The trailing value.
     * @return The training information of this feature.
     */
    public FeatureInfo trainFeature(TrainingEstimator estimates,
                                    FastCollection<IndexedPreference> ratings,
                                    double[] ufvs, double[] ifvs, double trail) {
        // Initialize our counters and error tracking
        StopWatch timer = new StopWatch();
        timer.start();

        double rmse = Double.MAX_VALUE;
        TrainingLoopController controller = getTrainingLoopController();
        while (controller.keepTraining(rmse)) {
            rmse = doFeatureIteration(estimates, ratings, ufvs, ifvs, trail);

            logger.trace("iteration {} finished with RMSE {}", controller.getIterationCount(), rmse);
        }

        timer.stop();
        logger.debug("Finished feature in {} epochs (took {})", controller.getIterationCount(), timer);

        Vec ufv = MutableVec.wrap(ufvs);
        Vec ifv = MutableVec.wrap(ifvs);

        return new FeatureInfo(ufv.mean(), ifv.mean(),
                               ufv.norm() * ifv.norm(),
                               controller.getIterationCount(),
                               rmse, controller.getLastDelta());
    }

    private double doFeatureIteration(TrainingEstimator estimates,
                                      FastCollection<IndexedPreference> ratings,
                                      double[] ufvs, double[] ifvs, double trail) {
        double sse = 0;
        int n = 0;

        for (IndexedPreference r: CollectionUtils.fast(ratings)) {
            final int uidx = r.getUserIndex();
            final int iidx = r.getItemIndex();

            // Step 1: Save the old feature values before computing the new ones
            final double ouf = ufvs[uidx];
            final double oif = ifvs[iidx];

            // Step 2: Compute the error
            final double err = computeError(r.getUserId(), r.getItemId(),
                                            trail, estimates.get(r),
                                            r.getValue(), ouf, oif);

            // Step 3: Update feature values
            ufvs[uidx] += userUpdate(err, ouf, oif);
            ifvs[iidx] += itemUpdate(err, ouf, oif);

            sse += err * err;
            n += 1;
        }

        return Math.sqrt(sse / n);
    }
}
