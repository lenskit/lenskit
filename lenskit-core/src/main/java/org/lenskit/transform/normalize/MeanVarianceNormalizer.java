/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.transform.normalize;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.inject.Shareable;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.math.Scalars;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
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
 */
@Shareable
public class MeanVarianceNormalizer extends AbstractVectorNormalizer implements Serializable {
    private static final long serialVersionUID = -7890335060797112954L;
    private static final Logger logger = LoggerFactory.getLogger(MeanVarianceNormalizer.class);

    private final double damping;
    private final double globalVariance;

    /**
     * Initializes basic normalizer with no damping.
     */
    @Inject
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
    public InvertibleFunction<Long2DoubleMap,Long2DoubleMap> makeTransformation(Long2DoubleMap reference) {
        if (reference.isEmpty()) {
            return new IdentityVectorNormalizer().makeTransformation(reference);
        } else {
            final double mean = Vectors.mean(reference);

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
            stdev = Scalars.isZero(sd) ? 1 : sd;
        }

        @Override
        public Long2DoubleMap unapply(Long2DoubleMap input) {
            if (input == null) return null;

            return Vectors.transform(input, (v) -> (mean + v * stdev));
        }

        @Nullable
        @Override
        public Long2DoubleMap apply(@Nullable Long2DoubleMap input) {
            if (input == null) return null;

            return Vectors.transform(input, (v) -> ((v - mean) / stdev));
        }

    }
}
