/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.transform.normalize;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.util.math.Scalars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;

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
 * <p>
 * If the reference vector has a standard deviation of 0 (as determined by {@link Scalars#isZero(double)}),
 * and there is no smoothing, then no scaling is done (it is treated as if the standard deviation is 1). This
 * is to keep the behavior well-defined in all cases.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(MeanVarianceNormalizer.Builder.class)
@Shareable
public class MeanVarianceNormalizer extends AbstractVectorNormalizer implements Serializable {
    private static final long serialVersionUID = -7890335060797112954L;
    private static final Logger logger = LoggerFactory.getLogger(MeanVarianceNormalizer.class);

    /**
     * A Builder for UserVarianceNormalizers that computes the variance from a
     * RatingBuildContext.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder implements Provider<MeanVarianceNormalizer> {
        private final double damping;
        private final EventDAO dao;

        /**
         * Create a new mean-variance normalizer builder.
         *
         * @param dao The DAO from which to get the global mean.
         * @param d   A Bayesian damping term.  The normalizer pretends each user has an
         *            additional <var>d</var> ratings that are equal to the global mean.
         */
        @Inject
        public Builder(@Transient EventDAO dao,
                       @MeanDamping double d) {
            Preconditions.checkArgument(d >= 0, "damping cannot be negative");
            this.dao = dao;
            damping = d;
        }

        @Override
        public MeanVarianceNormalizer get() {
            double variance = 0;

            if (damping > 0) {
                double sum = 0;

                Cursor<Rating> ratings = dao.streamEvents(Rating.class);
                int numRatings = 0;
                for (Rating r : ratings) {
                    if (r.hasValue()) {
                        sum += r.getValue();
                        numRatings++;
                    }
                }
                ratings.close();
                final double mean = sum / numRatings;

                ratings = dao.streamEvents(Rating.class);
                sum = 0;

                for (Rating r : ratings) {
                    if (r.hasValue()) {
                        double delta = mean - r.getValue();
                        sum += delta * delta;
                    }
                }
                ratings.close();
                variance = sum / numRatings;
            }

            return new MeanVarianceNormalizer(damping, variance);
        }
    }

    private final double damping;
    private final double globalVariance;

    /**
     * Initializes basic normalizer with no damping.
     */
    public MeanVarianceNormalizer() {
        this(0, 0);
    }

    /**
     * Construct a new mean variance normalizer.
     *
     * @param damping      damping factor to use. 0 for no damping, 5 for Hofmann's implementation.
     * @param globalVariance global variance to use in the damping calculations.
     */
    public MeanVarianceNormalizer(double damping, double globalVariance) {
        Preconditions.checkArgument(damping >= 0, "damping cannot be negative");
        this.damping = damping;
        this.globalVariance = globalVariance;
    }

    /**
     * Get the damping term.
     *
     * @return The damping term.
     */
    public double getDamping() {
        return damping;
    }

    /**
     * Get the global variance.
     *
     * @return The global variance from build time.
     */
    public double getGlobalVariance() {
        return globalVariance;
    }

    @Override
    public VectorTransformation makeTransformation(SparseVector reference) {
        if (reference.isEmpty()) {
            return new IdentityVectorNormalizer().makeTransformation(reference);
        } else {
            final double mean = reference.mean();

            double var = 0;
            DoubleIterator iter = reference.values().iterator();
            while (iter.hasNext()) {
                final double v = iter.nextDouble();
                final double diff = v - mean;
                var += diff * diff;
            }

            if (Scalars.isZero(var) && Scalars.isZero(damping)) {
                logger.warn("found zero variance for {}, and no damping is enabled", reference);
            }

            /* damping calculation as described in Hofmann '04
             * $\sigma_u^2 = \frac{\sigma^2 + q * \={\sigma}^2}{n_u + q}$
             */
            double stdev = Math.sqrt((var + damping * globalVariance) / (reference.size() + damping));
            return new Transform(mean, stdev);
        }
    }

    class Transform implements VectorTransformation {
        private final double mean;
        private final double stdev;

        public Transform(double m, double sd) {
            mean = m;
            stdev = sd;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            double recipSD = Scalars.isZero(stdev) ? 1 : (1 / stdev);
            for (VectorEntry rating : vector) {
                vector.set(rating, (rating.getValue() - mean) * recipSD);
            }
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            for (VectorEntry rating : vector) {
                double val = rating.getValue();
                if (!Scalars.isZero(stdev)) {
                    val *= stdev;
                }
                vector.set(rating, val + mean);
            }
            return vector;
        }
    }
}
