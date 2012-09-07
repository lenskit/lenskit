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

import javax.annotation.concurrent.NotThreadSafe;

import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.util.iterative.StoppingCondition;

/**
 * Computes updates for FunkSVD feature training rounds.
 *
 * @since 1.0
 */
@NotThreadSafe
public final class FunkSVDFeatureTrainer {
    private int epoch;

    private int ratingCount;
    private double ssq;
    private double oldRmse;
    private double rmse;
    private final double MIN_EPOCHS;

    private double err;
    private double ufv;
    private double ifv;

    private final double learningRate;
    private final double trainingRegularization;
    private final ClampingFunction clampingFunction;
    private final StoppingCondition stopper;

    public FunkSVDFeatureTrainer(FunkSVDTrainingConfig conf) {
        epoch = 0;
        ratingCount = 0;
        err = 0.0;
        ssq = 0.0;
        oldRmse = 0.0;
        rmse = Double.MAX_VALUE;
        ufv = 0.0;
        ifv = 0.0;
        MIN_EPOCHS = 50;

        learningRate = conf.getLearningRate();
        trainingRegularization = conf.getTrainingRegularization();
        clampingFunction = conf.getClampingFunction();
        stopper = conf.getStoppingCondition();
    }


    public void compute(long uid, long iid, double trailingValue,
                        double estimate, double rating, double ufv, double ifv) {

        // Store the new feature values
        this.ufv = ufv;
        this.ifv = ifv;

        // Compute prediction
        double pred = estimate + ufv * ifv;

        // Clamp the prediction first
        pred = clampingFunction.apply(uid, iid, pred);

        // Add the trailing value, then clamp the result again
        pred = clampingFunction.apply(uid, iid, pred + trailingValue);

        // Compute the err and store this value
        err = rating - pred;

        // Update properties
        ssq += (rating - pred) * (rating - pred);

        // Keep track of how many ratings have been gone through
        ratingCount += 1;
    }


    public double getUserUpdate() {
        double delta = err * ifv - trainingRegularization * ufv;
        return delta * learningRate;
    }

    public double getItemUpdate() {
        double delta = err * ufv - trainingRegularization * ifv;
        return delta * learningRate;
    }

    public int getEpoch() {
        return epoch;
    }

    public double getLastRMSE() {
        return rmse;
    }

    public boolean nextEpoch() {
        if (ratingCount > 0) {
            oldRmse = rmse;
            rmse = Math.sqrt(ssq / ratingCount);
            ssq = 0;
        }

        if (!stopper.isFinished(epoch, oldRmse - rmse)) {
            epoch += 1;
            ratingCount = 0;
            return true;
        }

        return false;
    }
}
