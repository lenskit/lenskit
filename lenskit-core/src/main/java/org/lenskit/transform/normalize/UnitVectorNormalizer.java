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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.inject.Shareable;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.math.Vectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Vector normalizer that scales a vector by the factor needed to scale the
 * reference vector to a unit vector. If the length of the reference vector
 * is 0, no normalization is applied.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class UnitVectorNormalizer extends AbstractVectorNormalizer implements Serializable {
    private final static long serialVersionUID = 1L;
    private final double tolerance;

    /**
     * Create a unit vector normalizer with a tolerance of 1.0e-6.
     */
    @Inject
    public UnitVectorNormalizer() {
        this(1.0e-6);
    }

    /**
     * Create a unit vector normalizer a specified tolerance around 0. Any
     * vector norm whose absolute value is less than <var>t</var> is converted
     * to a no-op.
     *
     * @param t The error tolerance for 0-checking.
     */
    public UnitVectorNormalizer(double t) {
        tolerance = t;
    }

    @Override
    public InvertibleFunction<Long2DoubleMap, Long2DoubleMap> makeTransformation(Long2DoubleMap reference) {
        double s = Vectors.euclideanNorm(reference);
        if (Math.abs(s) < tolerance) {
            return new IdentityVectorNormalizer().makeTransformation(reference);
        } else {
            return new ScalingTransform(s);
        }
    }

    static class ScalingTransform implements VectorTransformation {
        final double factor;

        public ScalingTransform(double f) {
            factor = f;
        }

        @Override
        public Long2DoubleMap unapply(Long2DoubleMap input) {
            return input == null ? null : Vectors.multiplyScalar(input, factor);
        }

        @Nullable
        @Override
        public Long2DoubleMap apply(@Nullable Long2DoubleMap input) {
            return input == null ? null : Vectors.multiplyScalar(input, 1.0 / factor);
        }

    }
}
