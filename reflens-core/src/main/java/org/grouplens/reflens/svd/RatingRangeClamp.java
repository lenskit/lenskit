package org.grouplens.reflens.svd;

import org.grouplens.reflens.params.MaxRating;
import org.grouplens.reflens.params.MinRating;
import org.grouplens.reflens.util.DoubleFunction;

import com.google.inject.Inject;

public final class RatingRangeClamp implements DoubleFunction {

	private final double minRating, maxRating;
	
	@Inject
	RatingRangeClamp(@MinRating double min, @MaxRating double max) {
		minRating = min;
		maxRating = max;
	}
	
	@Override
	public double apply(double v) {
		if (v < minRating) return minRating;
		else if (v > maxRating) return maxRating;
		else return v;
	}

}
