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
package org.grouplens.lenskit.norm;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;

import java.io.Serializable;

import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.params.MeanSmoothing;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * <p>
 * Normalizes against the variance of the vector with optional smoothing as
 * described in Hofmann '04.
 * <p>
 * For user rating vectors, this normalization assumes that a user's mean rating
 * and variance are independent of actual preferences, and attempts to describe
 * the preference of a rating by the distance of the rating from the mean,
 * relative to the user's normal rating variance.
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
public class MeanVarianceNormalizer extends AbstractVectorNormalizer<ImmutableSparseVector> implements Serializable {
    private static final long serialVersionUID = -7890335060797112954L;

    /**
     * A Builder for UserVarianceNormalizers that computes the variance from a
     * RatingBuildContext.
     *
     * @author Michael Ludwig
     */
    public static class Builder extends RecommenderComponentBuilder<MeanVarianceNormalizer> {
        private double smoothing;

        // FIXME should this be the MeanSmoothing parameter (which is used by the baselines?)
        @MeanSmoothing
        public void setSmoothing(double smoothing) {
            this.smoothing = smoothing;
        }

        @Override
        public MeanVarianceNormalizer build() {
            double variance = 0;

            if (smoothing != 0) {
                double mean = 0;
                double sum = 0;
                FastCollection<IndexedPreference> ratings = snapshot.getRatings();
                int numRatings = ratings.size();
                for (IndexedPreference rating : ratings.fast()) {
                    sum += rating.getValue();
                }
                mean = sum / numRatings;
                sum = 0;
                for (IndexedPreference rating : ratings.fast()) {
                    double delta = mean - rating.getValue();
                    sum += delta * delta;
                }
                variance = sum / numRatings;
            }

            return new MeanVarianceNormalizer(smoothing, variance);
        }
    }

    private final double smoothing;
    private final double globalVariance;

    /**
     * Initializes basic normalizer with no smoothing.
     */
    public MeanVarianceNormalizer() {
        this(0, 0);
    }

    /**
     * @param smoothing            smoothing factor to use. 0 for no smoothing, 5 for Hofmann's implementation.
     * @param globalVariance    global variance to use in the smoothing calculations.
     */
    public MeanVarianceNormalizer(double smoothing, double globalVariance) {
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
    public VectorTransformation makeTransformation(ImmutableSparseVector reference) {
        if (reference.isEmpty())
            return new IdentityVectorNormalizer().makeTransformation(reference);
        return new Transform(reference);
    }

    class Transform implements VectorTransformation {
        private final double mean;
        private final double stdDev;

        public Transform(ImmutableSparseVector reference) {
            final double m = mean = reference.mean();

            double var = 0;
            DoubleIterator iter = reference.values().iterator();
            while (iter.hasNext()) {
                final double v = iter.nextDouble();
                final double diff = v - m;
                var += diff * diff;
            }

            /* smoothing calculation as described in Hofmann '04
             * $\sigma_u^2 = \frac{\sigma^2 + q * \={\sigma}^2}{n_u + q}$
             */
            stdDev = Math.sqrt((var + smoothing * globalVariance) / (reference.size() + smoothing));
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            for (Entry rating : vector.fast()) {
                vector.set(rating.getLongKey(), /* r' = (r - u) / s */
                        stdDev == 0? 0 : // edge case
                            (rating.getDoubleValue() - mean) / stdDev);
            }
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            for (Entry rating : vector.fast()) {
                vector.set(rating.getLongKey(), /* r = r' * s + u */
                        stdDev == 0? mean : // edge case
                        (rating.getDoubleValue() * stdDev) + mean);
            }
            return vector;
        }
    }
}
