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
package org.grouplens.lenskit.transform.normalize;

import java.io.Serializable;

import javax.inject.Inject;

import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Shareable
public class BaselineSubtractingUserVectorNormalizer extends AbstractUserVectorNormalizer implements Serializable {
    private static final long serialVersionUID = 2L;

    protected final BaselinePredictor baselinePredictor;

    /**
     * Create a new BaselineSubtractingUserVectorNormalizer with the given baseline.
     *
     * @param baseline The baseline predictor to use for normalization.
     */
    @Inject
    public BaselineSubtractingUserVectorNormalizer(BaselinePredictor baseline) {
        baselinePredictor = baseline;
    }

    @Override
    public VectorTransformation makeTransformation(long user, SparseVector ratings) {
        if (ratings.isEmpty()) {
            return new IdentityVectorNormalizer().makeTransformation(ratings);
        }
        return new Transformation(user, ratings);
    }

    protected class Transformation implements VectorTransformation {
        private final long user;
        private final SparseVector vector;

        public Transformation(long u, SparseVector r) {
            user = u;
            vector = r;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            SparseVector base = baselinePredictor.predict(user, this.vector, vector.keySet());
            vector.subtract(base);
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            SparseVector base = baselinePredictor.predict(user, this.vector, vector.keySet());
            vector.add(base);
            return vector;
        }
    }

    @Override
    public String toString() {
        return String.format("[BaselineNorm: %s]", baselinePredictor);
    }
}
