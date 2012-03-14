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

import java.io.Serializable;

import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Identity normalization (makes no change).
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IdentityVectorNormalizer extends AbstractVectorNormalizer<ImmutableSparseVector>
implements Serializable {
    private static final long serialVersionUID = -6708410675383598691L;

    private static final VectorTransformation IDENTITY_TRANSFORM = new VectorTransformation() {

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            return vector;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            return vector;
        }
    };

    @Override
    public VectorTransformation makeTransformation(ImmutableSparseVector ratings) {
        return IDENTITY_TRANSFORM;
    }
}
