/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
