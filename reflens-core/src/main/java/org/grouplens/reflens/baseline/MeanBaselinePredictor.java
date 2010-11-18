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
public class MeanBaselinePredictor extends ConstantBaselinePredictor {
	private static final Logger logger = LoggerFactory.getLogger(MeanBaselinePredictor.class);

	/**
	 * @param value
	 */
	private MeanBaselinePredictor(double value) {
		super(value);
	}

	public static class Builder implements RatingPredictorBuilder {

		@Override
		public RatingPredictor build(RatingDataSource data) {
			double total = 0;
			long count = 0;
			Cursor<Rating> ratings = data.getRatings();
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
			return new MeanBaselinePredictor(avg);
		}
		
	}
}
