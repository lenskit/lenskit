/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.mf.funksvd;

import org.lenskit.data.ratings.PreferenceDomain;

/**
 * Encapsulation of the FunkSVD update process.  Using this class takes a two-step process:
 *
 * <ol>
 *     <li>Call {@link #prepare(int, double, double, double, double, double)} to prepare
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
public final class FunkSVDTrainingUpdater {
    private final FunkSVDUpdateRule updateRule;

    private double error;
    private double userFeatureValue;
    private double itemFeatureValue;
    private double sse;
    private int n;

    FunkSVDTrainingUpdater(FunkSVDUpdateRule rule) {
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
     * @param uv       The user feature value.
     * @param iv       The item feature value.
     * @param trail    The sum of the trailing feature value products.
     */
    public void prepare(int feature, double rating, double estimate,
                        double uv, double iv, double trail) {
        // Compute prediction
        double pred = estimate + uv * iv;
        PreferenceDomain dom = updateRule.getDomain();
        if (dom != null) {
            pred = dom.clampValue(pred);
        }
        pred += trail;

        // Compute the err and store this value
        error = rating - pred;
        userFeatureValue = uv;
        itemFeatureValue = iv;

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
