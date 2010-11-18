/**
 * 
 */
package org.grouplens.reflens.baseline;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.Collection;
import java.util.Map;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.util.CollectionUtils;

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
		DoubleCollection fvalues = CollectionUtils.getFastCollection(values);
		DoubleIterator iter = fvalues.iterator();
		while (iter.hasNext()) {
			total += iter.nextDouble() - offset;
		}
		return total / values.size();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
	 */
	@Override
	public Map<Long, Double> predict(long user, Map<Long, Double> ratings,
			Collection<Long> items) {
		double mean = average(ratings, globalMean) + globalMean;
		Long2DoubleMap map = new Long2DoubleOpenHashMap(items.size());
		LongCollection fitems = CollectionUtils.getFastCollection(items);
		LongIterator iter = fitems.iterator();
		while (iter.hasNext()) {
			map.put(iter.nextLong(), mean);
		}
		return map;
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
