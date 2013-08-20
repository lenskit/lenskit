/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * User vector normalizer that subtracts a user's baseline scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class BaselineSubtractingUserVectorNormalizer extends AbstractUserVectorNormalizer implements Serializable {
    private static final long serialVersionUID = 3L;

    protected final ItemScorer baselineScorer;

    /**
     * Create a new baseline-subtracting normalizer with the given baseline.
     *
     * @param baseline The baseline scorer to use for normalization.
     */
    @Inject
    public BaselineSubtractingUserVectorNormalizer(@BaselineScorer ItemScorer baseline) {
        baselineScorer = baseline;
    }

    @Override
    public VectorTransformation makeTransformation(long user, SparseVector ratings) {
        return new Transformation(user);
    }

    private class Transformation implements VectorTransformation {
        private final long user;

        public Transformation(long u) {
            user = u;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            MutableSparseVector base = new MutableSparseVector(vector.keySet());
            baselineScorer.score(user, base);
            vector.subtract(base);
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            MutableSparseVector base = new MutableSparseVector(vector.keySet());
            baselineScorer.score(user, base);
            vector.add(base);
            return vector;
        }
    }

    @Override
    public String toString() {
        return String.format("[BaselineNorm: %s]", baselineScorer);
    }
}
