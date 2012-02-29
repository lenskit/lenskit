/*
 * LensKit, an open source recommender systems toolkit.
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

import java.io.Serializable;

import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.params.NormalizerBaseline;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class BaselineSubtractingNormalizer extends AbstractVectorNormalizer<UserVector> implements Serializable {
    private static final long serialVersionUID = 1449043456567302903L;

    protected final BaselinePredictor baselinePredictor;

    /**
     * Create a new BaselineSubtractingNormalizer with the given baseline.
     *
     * @param baseline
     */
    public BaselineSubtractingNormalizer(@NormalizerBaseline BaselinePredictor baseline) {
        baselinePredictor = baseline;
    }

    @Override
    public VectorTransformation makeTransformation(UserVector ratings) {
        if (ratings.isEmpty())
            return new IdentityVectorNormalizer().makeTransformation(ratings);
        return new Transformation(ratings);
    }

    protected class Transformation implements VectorTransformation {
        private final UserVector user;

        public Transformation(UserVector r) {
            user = r;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            SparseVector base = baselinePredictor.predict(user, vector.keySet());
            vector.subtract(base);
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            SparseVector base = baselinePredictor.predict(user, vector.keySet());
            vector.add(base);
            return vector;
        }
    }

    @Override
    public String toString() {
        return String.format("[BaselineNorm: %s]", baselinePredictor);
    }
}
