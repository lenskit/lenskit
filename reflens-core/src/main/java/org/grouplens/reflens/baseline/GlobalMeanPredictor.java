package org.grouplens.reflens.baseline;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GlobalMeanPredictor extends ConstantPredictor {
	private static final Logger logger = LoggerFactory.getLogger(GlobalMeanPredictor.class);

	/**
	 * @param value
	 */
	private GlobalMeanPredictor(double value) {
		super(value);
	}
	
	/**
	 * Helper method to compute the mean of all ratings in a cursor.
	 * The cursor is closed after the ratings are computed.
	 * @param ratings A cursor of ratings to average.
	 * @return The arithemtic mean of all ratings.
	 */
	public static double computeMeanRating(Cursor<Rating> ratings) {
		double total = 0;
		long count = 0;
		try {
			for (Rating r: ratings) {
				total += r.getRating();
				count += 1;
			}
		} finally {
			ratings.close();
		}
		double avg = 0;
		if (count > 0)
			avg = total / count;
		return avg;
	}

	public static class Builder implements RatingPredictorBuilder {

		@Override
		public RatingPredictor build(RatingDataSource data) {
			return new GlobalMeanPredictor(computeMeanRating(data.getRatings()));
		}
		
	}
}
