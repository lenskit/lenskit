/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.norm;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.params.MeanSmoothing;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.util.FastCollection;

/**
 * <p>
 * Normalizes against the variance of the vector with optional smoothing as
 * described in Hofmann '04.
 * <p>
 * The normalization assumes that a user's mean rating and variance are
 * independent of actual preferences, and attempts to describe the preference of
 * a rating by the distance of the rating from the mean, relative to the user's
 * normal rating variance.
 * <p>
 * The smoothing factor helps to smooth out results for users with fewer ratings
 * by re-weighting the user's rating variance. The 'smoothing number' is a
 * number of 'default' ratings to give the user, weighting the user's variance
 * towards the average community variance. Accordingly, set smoothing = 0 (or
 * use default constructor) for no smoothing. The 'global variance' parameter
 * only pertains to smoothing, and is unnecessary otherwise.
 * 
 * @author Stefan Nelson-Lindall <stefan@cs.umn.edu>
 */
@Built
public class UserVarianceNormalizer extends AbstractUserRatingVectorNormalizer {
    private static final long serialVersionUID = -7890335060797112954L;

    /**
     * A Builder for UserVarianceNormalizers that computes the variance from a
     * RatingBuildContext.
     * 
     * @author Michael Ludwig
     */
    public static class Builder extends RecommenderComponentBuilder<UserVarianceNormalizer> {
        private double smoothing;

        // FIXME should this be the MeanSmoothing parameter (which is used by the baselines?)
        @MeanSmoothing
        public void setSmoothing(double smoothing) {
            this.smoothing = smoothing;
        }
        
        @Override
        public UserVarianceNormalizer build() {
            double variance = 0;
            
            if (smoothing != 0) {
                double mean = 0;
                double sum = 0;
                FastCollection<IndexedRating> ratings = snapshot.getRatings();
                int numRatings = ratings.size();
                for (IndexedRating rating : ratings.fast()) {
                    sum += rating.getRating();
                }
                mean = sum / numRatings;
                sum = 0;
                for (IndexedRating rating : ratings.fast()) {
                    double delta = mean - rating.getRating();
                    sum += delta * delta;
                }
                variance = sum / numRatings;
            }
            
            return new UserVarianceNormalizer(smoothing, variance);
        }
    }
    
	private final double smoothing;
	private final double globalVariance;
	
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
	
	public double getSmoothing() {
	    return smoothing;
	}
	
	public double getGlobalVariance() {
	    return globalVariance;
	}

	@Override
	public VectorTransformation makeTransformation(long userId, final SparseVector ratings) {
		final double userMean = ratings.mean();

		/* smoothing calculation as described in Hofmann '04 
		 * $\sigma_u^2 = \frac{\sigma^2 + q * \={\sigma}^2}{n_u + q}$
		 */
		double sum = 0;
		for (double rating : ratings.values()) {
		    double diff = userMean - rating;
			sum += diff * diff;
		}
		final double userStdDev = Math.sqrt((sum + smoothing * globalVariance) / (ratings.size() + smoothing));
		
		return new VectorTransformation() {
			@Override
			public MutableSparseVector apply(MutableSparseVector vector) {
				for (Entry rating : vector.fast()) {
					vector.set(rating.getLongKey(), /* r' = (r - u) / s */
							userStdDev == 0? 0 : // edge case
								(rating.getDoubleValue() - userMean) / userStdDev);
				}
				return vector;
			}

			@Override
			public MutableSparseVector unapply(MutableSparseVector vector) {
				for (Entry rating : vector.fast()) {
					vector.set(rating.getLongKey(), /* r = r' * s + u */
							userStdDev == 0? userMean : // edge case
							(rating.getDoubleValue() * userStdDev) + userMean);
				}
				return vector;
			}
		};
	}
}
