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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
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
    public VectorTransformation makeTransformation(SparseVector reference) {
        double s = reference.norm();
        if (Math.abs(s) < tolerance) {
            return new IdentityVectorNormalizer().makeTransformation(reference);
        } else {
            return new ScalingTransform(s);
        }
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
        public MutableSparseVector apply(MutableSparseVector vector) {
            vector.multiply(1 / factor);
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            vector.multiply(factor);
            return vector;
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
        @Override
        public double apply(long key, double value) {
            return value / factor;
        }

        @Override
        public double unapply(long key, double value) {
            return value * factor;
        }
    }
}
