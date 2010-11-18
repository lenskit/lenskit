/**
 * 
 */
package org.grouplens.reflens.baseline;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;

import java.util.Collection;
import java.util.Map;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;

import com.google.inject.Inject;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserMeanBaselinePredictor implements RatingPredictor {
	private final double globalMean;
	
	UserMeanBaselinePredictor(double mean) {
		globalMean = mean;
	}
	
	static double average(Map<Long,Double> ratings, double offset) {
		if (ratings.isEmpty()) return 0;
		
		Collection<Double> values = ratings.values();
		double total = 0;
		if (values instanceof DoubleCollection) {
			DoubleIterator iter = ((DoubleCollection) values).iterator();
			while (iter.hasNext()) {
				total += iter.nextDouble() - offset;
			}
		} else {
			for (double v: values) {
				total += v - offset;
			}
		}
		return total / values.size();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
	 */
	@Override
	public Map<Long, Double> predict(long user, Map<Long, Double> ratings,
			Collection<Long> items) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, java.util.Map, long)
	 */
	@Override
	public ScoredId predict(long user, Map<Long, Double> ratings, long item) {
		return new ScoredId(item, average(ratings, globalMean) + globalMean);
	}
	
	public static class Builder implements RatingPredictorBuilder {
		@Inject
		public Builder() {
		}
		@Override
		public RatingPredictor build(RatingDataSource data) {
			double avg = MeanBaselinePredictor.computeMeanRating(data.getRatings());
			return new UserMeanBaselinePredictor(avg);
		}
	}
}
