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

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Vector normalizer that scales a vector by the factor needed to scale the
 * reference vector to a unit vector. If the length of the reference vector
 * is 0, no normalization is applied.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
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
    
    static class ScalingTransform implements VectorTransformation {
        final double factor;
        
        public ScalingTransform(double f) {
            factor = f;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            vector.scale(1/factor);
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            vector.scale(factor);
            return vector;
        }
        
    }
}
