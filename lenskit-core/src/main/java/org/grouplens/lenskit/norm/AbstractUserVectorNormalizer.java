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

import javax.annotation.Nullable;

/**
 * Abstract user vector normalizer implementation.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @since 0.11
 */
public abstract class AbstractUserVectorNormalizer implements UserVectorNormalizer {

    /**
     * Implementation that delegates to {@link #makeTransformation(long, SparseVector)}
     * and the resulting {@link org.grouplens.lenskit.norm.VectorTransformation}.
     */
    @Override
    public MutableSparseVector normalize(long user, SparseVector vector,
                                         @Nullable MutableSparseVector target) {
        if (target == null) {
            target = vector.mutableCopy();
        }

        VectorTransformation tform = makeTransformation(user, vector);
        return tform.apply(target);
    }
}
