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

import org.grouplens.lenskit.vectors.Vec;

/**
 * Encapsulation of the FunkSVD update process.  Using this class takes a two-step process:
 *
 * <ol>
 *     <li>Call {@link #prepare(int, double, double, org.grouplens.lenskit.vectors.Vec, org.grouplens.lenskit.vectors.Vec)} to prepare
 *     an update.</li>
 *     <li>Call {@link #getItemFeatureUpdate()} and {@link #getUserFeatureUpdate()} to get the
 *     deltas to apply to the item-feature and user-feature values, respectively.</li>
 * </ol>
 *
 * <p>The updater can be reused for multiple updates, but cannot be shared between threads.  It
 * is typical to create one updater and reuse it for much of the training process.</p>
 *
 * <p>The updater also tracks statistics across runs.</p>
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class FunkSVDUpdater {
    private final FunkSVDUpdateRule updateRule;

    private double error;
    private double userFeatureValue;
    private double itemFeatureValue;
    private double sse;
    private int n;

    FunkSVDUpdater(FunkSVDUpdateRule rule) {
        updateRule = rule;
    }

    /**
     * Reset the statistics and counters tracked by this updater.
     */
    public void resetStatistics() {
        sse = 0;
        n = 0;
    }

    /**
     * Get the number of updates this updater has prepared since the last reset.
     * @return The number of updates done.
     * @see #resetStatistics()
     */
    public int getUpdateCount() {
        return n;
    }

    /**
     * Get the RMSE of all updates done since the last reset.
     * @return The root-mean-squared error of the updates since the last reset.
     */
    public double getRMSE() {
        if (n <= 0) {
            return Double.NaN;
        } else {
            return Math.sqrt(sse / n);
        }
    }

    /**
     * Prepare the updater for updating the feature values for a particular user/item ID.
     *
     * @param feature  The feature we are training.
     * @param rating   The rating value.
     * @param estimate The estimate through the previous feature.
     * @param uvec     The item feature vector for remaining features.
     * @param ivec     The user feature vector for remaining features.
     */
    public void prepare(int feature, double rating, double estimate, Vec uvec, Vec ivec) {
        // Compute prediction
        double pred = updateRule.getKernel().apply(estimate, uvec, ivec);

        // Compute the err and store this value
        error = rating - pred;
        userFeatureValue = uvec.get(feature);
        itemFeatureValue = ivec.get(feature);

        // Update statistics
        n += 1;
        sse += error * error;
    }

    /**
     * Get the error from the prepared update.
     * @return The estimation error in the prepared update.
     */
    public double getError() {
        return error;
    }

    /**
     * Get the update for the user-feature value.
     * @return The delta to apply to the user-feature value.
     */
    public double getUserFeatureUpdate() {
        double delta = error * itemFeatureValue - updateRule.getTrainingRegularization() * userFeatureValue;
        return delta * updateRule.getLearningRate();
    }

    /**
     * Get the update for the item-feature value.
     * @return The delta to apply to the item-feature value.
     */
    public double getItemFeatureUpdate() {
        double delta = error * userFeatureValue - updateRule.getTrainingRegularization() * itemFeatureValue;
        return delta * updateRule.getLearningRate();
    }
}
