package org.grouplens.lenskit.svd;

import org.grouplens.lenskit.transform.clamp.ClampingFunction;

public final class UpdateRule {
	private int epoch;
	private int ratingCount;
	private double err;
	private double ssq;
	private double oldRmse;
	private double rmse;
	
	private final double MIN_EPOCHS;
	private final double iterationCount;
    private final double learningRate;
    private final double trainingThreshold;
    private final double trainingRegularization;
    private final ClampingFunction clampingFunction;
    
	
	public UpdateRule(double learningRate, double threshold, double gradientDescent,
						int iterationCount, ClampingFunction clamp, double MIN_EPOCHS) {
		epoch = 0;
		ratingCount = 0;
		err = 0.0;
		ssq = 0.0;
		oldRmse = 0.0;
		rmse = Double.MAX_VALUE;
		
		this.MIN_EPOCHS = MIN_EPOCHS;
		this.learningRate = learningRate;
		trainingThreshold = threshold;
		trainingRegularization = gradientDescent;
		clampingFunction = clamp;
		this.iterationCount = iterationCount;
		
	}
	
	private void ratingCountIncrement() {
		ratingCount += 1;
	}
	
	private void epochIncrement() {
		epoch += 1;
	}
	
	private void updateErr(double newValue) {
		err = newValue;
	}
	
	private void updateSsq(double newValue) {
		ssq += newValue;
	}
	
	public void compute(long uid, long iid, double trailingValue, 
			double estimate, double rating, double ufv, double ifv) {
		
		// Store incoming feature values, then compute prediction
		double pred = estimate + ufv * ifv;
		
		// Clamp the prediction first
		pred = clampingFunction.apply(uid, iid, pred);
		
		// Add the trailing value, then clamp the result again
		pred = clampingFunction.apply(uid, iid, pred + trailingValue);
		
		// Compute the err and store this value
		err = rating - pred;
		updateErr(err);
		// Update the ssq
		updateSsq(err * err);
		
		// Keep track of how many ratings have been gone through
		ratingCountIncrement();
	}
	
	public double getUserUpdate(double ufv, double ifv) {
		double delta = err * ifv - trainingRegularization * ufv;
		return ufv + delta * learningRate;
	}
	
	public double getItemUpdate(double ufv, double ifv) {
		double delta = err * ufv - trainingRegularization * ifv;
		return ifv + delta * learningRate;
	}
	
	public double getLastErr() {
		return err;
	}
	
	public double getCurrentSsq() {
		return ssq;
	}
	
	public int getEpoch() {
		return epoch;
	}
	
	public double getRmse() {
		return rmse;
	}
	
	private void resetRatingCount() {
		ratingCount = 0;
	}
	
	public void reset() {
		epoch = 0;
		err = 0;
		ssq = 0;
	}
	
	private boolean isDone(int epoch, double rmse, double oldRmse) {
        if (iterationCount > 0) {
            return epoch >= iterationCount;
        } else {
            return epoch >= MIN_EPOCHS && (oldRmse - rmse) < trainingThreshold;
        }
    }
	
	public boolean nextEpochs() {
		if (!isDone(epoch, rmse, oldRmse)) {
			oldRmse = rmse;
			rmse = Math.sqrt(ssq / ratingCount);
			epochIncrement();
			resetRatingCount();
			return true;
		} else {
			return false;
		}
	}
	
}
