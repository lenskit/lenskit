package org.grouplens.lenskit.norm;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;

import java.util.Collection;

import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Normalizes against the variance of the vector with optional smoothing as described
 * 	in Hofmann '04. 
 * 
 * The normalization assumes that a user's mean rating and variance are independent of
 * 	actual preferences, and attempts to describe the preference of a rating by the
 * 	distance of the rating from the mean, relative to the user's normal rating variance.
 * 
 * The smoothing factor helps to smooth out results for users with fewer ratings by
 * 	re-weighting the user's rating variance. The 'smoothing number' is a number of
 * 	'default' ratings to give the user, weighting the user's variance towards the
 * 	average community variance. Accordingly, set smoothing = 0 (or use default
 * 	constructor) for no smoothing. The 'global variance' parameter only pertains to
 * 	smoothing, and is unnecessary otherwise.
 * 
 * @author Stefan Nelson-Lindall <stefan@cs.umn.edu>
 *
 */
public class UserVarianceNormalizer extends AbstractUserRatingVectorNormalizer {
	
	final double smoothing;
	final double globalVariance;
	
	/**
	 * Initializes basic normalizer with no smoothing.
	 */
	public UserVarianceNormalizer() {
		this(0, 0);
	}

	/**
	 * @param smoothing			smoothing factor to use. 0 for no smoothing, 5 for Hofmann's implementation.
	 * @param globalVariance	global variance to use in the smoothing calculations.
	 */
	public UserVarianceNormalizer(double smoothing, double globalVariance) {
		this.smoothing = smoothing;
		this.globalVariance = globalVariance;
	}
	
	/**
	 * @param smoothing			smoothing factor to use. 0 for no smoothing, 5 for Hofmann's implementation.
	 * @param ratings			used to calculate global variance for use in smoothing calculations.
	 */
	public UserVarianceNormalizer(double smoothing, RatingBuildContext ratings) {
		this.smoothing = smoothing;
		
		// Don't bother wasting cycles if not smoothing
		if (smoothing == 0) {
			this.globalVariance = 0;
			return;
		}
		
		double mean = 0;
		double variance = 0;
		double sum = 0;
		Collection<IndexedRating> fastRatings = ratings.getRatings();
		int numRatings = fastRatings.size();
		for (IndexedRating rating : fastRatings) {
			sum += rating.getRating();
		}
		mean = sum / numRatings;
		sum = 0;
		for (IndexedRating rating : fastRatings) {
			sum += Math.pow(mean - rating.getRating(), 2);
		}
		variance = sum / numRatings;
		
		this.globalVariance = variance;
	}

	@Override
	public VectorTransformation makeTransformation(long userId,
			final SparseVector ratings) {
		
		final double userMean = ratings.mean();
		final double userStdDev;

		/* smoothing calculation as described in Hofmann '04 
		 * $\sigma_u^2 = \frac{\sigma^2 + q * \={\sigma}^2}{n_u + q}$
		 */
		double sum = 0;
		for (double rating : ratings.values()) {
			sum += Math.pow(userMean - rating, 2);
		}
		userStdDev = Math.sqrt((sum + smoothing * globalVariance) / (ratings.size() + smoothing));
		
		return new VectorTransformation() {

			@Override
			public void apply(MutableSparseVector vector) {
				for (Entry rating : vector.fast()) {
					vector.set(rating.getLongKey(), /* r' = (r - u) / s */
							userStdDev == 0? 0 : // edge case
								(rating.getDoubleValue() - userMean) / userStdDev);
				}
			}

			@Override
			public void unapply(MutableSparseVector vector) {
				for (Entry rating : vector.fast()) {
					vector.set(rating.getLongKey(), /* r = r' * s + u */
							userStdDev == 0? userMean : // edge case
							(rating.getDoubleValue() * userStdDev) + userMean);
				}
			}
			
		};
	}

}
