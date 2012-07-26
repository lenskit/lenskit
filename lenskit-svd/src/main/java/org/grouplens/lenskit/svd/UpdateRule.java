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

public final class UpdateRule {
	private int epoch;

    private int ratingCount;
	private double ssq;
	private double oldRmse;
	private double rmse;

    private double err;
	private double ufv;
	private double ifv;
	
	private final double MIN_EPOCHS;
	private final double iterationCount;
    private final double learningRate;
    private final double trainingThreshold;
    private final double trainingRegularization;
    private final ClampingFunction clampingFunction;
    
    
	@Inject
	public UpdateRule(@LearningRate double rate, @TrainingThreshold double threshold,
            	@RegularizationTerm double gradientDescent, ClampingFunction clamp,
            	@IterationCount int iterCount) {
		epoch = 0;
		ratingCount = 0;
		err = 0.0;
		ssq = 0.0;
		oldRmse = 0.0;
		rmse = Double.MAX_VALUE;
		ufv = 0.0;
		ifv = 0.0;
		
		MIN_EPOCHS = 50;
		learningRate = rate;
		trainingThreshold = threshold;
		trainingRegularization = gradientDescent;
		clampingFunction = clamp;
		iterationCount = iterCount;
		
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
	
	public void reset() {
		epoch = 0;
		err = 0.0;
		ssq = 0.0;
	}
	
	public boolean nextEpoch() {
        if (ratingCount > 0) {
            oldRmse = rmse;
            rmse = Math.sqrt(ssq / ratingCount);
            ssq = 0;
        }

        if (!isDone(epoch, rmse, oldRmse)) {
			epoch += 1;
			ratingCount = 0;
			return true;
		} 
		
		return false;
	}
	
	private boolean isDone(int epoch, double rmse, double oldRmse) {
        if (iterationCount > 0) {
            return epoch >= iterationCount;
        } else {
            return epoch >= MIN_EPOCHS && (oldRmse - rmse) < trainingThreshold;
        }
    }
}
