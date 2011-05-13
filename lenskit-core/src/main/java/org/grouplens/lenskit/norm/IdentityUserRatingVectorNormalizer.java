/*
 * LensKit, a reference implementation of recommender algorithms.
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

import org.grouplens.lenskit.AbstractRecommenderComponentBuilder;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Identity normalization (makes no change).
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IdentityUserRatingVectorNormalizer extends
        AbstractUserRatingVectorNormalizer {
    /**
     * Builder for the IdentityUserRatingVectorNormalizer.
     * 
     * @author Michael Ludwig
     */
    public static class Builder extends AbstractRecommenderComponentBuilder<IdentityUserRatingVectorNormalizer> {

        @Override
        protected IdentityUserRatingVectorNormalizer buildNew(RatingBuildContext context) {
            return new IdentityUserRatingVectorNormalizer();
        }
    }
    
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

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.norm.UserRatingVectorNormalizer#makeTransformation(long, org.grouplens.lenskit.data.vector.SparseVector)
     */
    @Override
    public VectorTransformation makeTransformation(long userId,
            SparseVector ratings) {
        return IDENTITY_TRANSFORM;
    }

}
