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
import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ConstantPredictor;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.vector.ImmutableSparseVector;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class BaselineSubtractingNormalizer extends AbstractUserRatingVectorNormalizer {
    /**
     * Builder for BaselineSubtractingNormalizer. Its sole parameter takes a
     * builder for a BaselinePredictor.
     * 
     * @author Michael Ludwig
     */
    public static class Builder extends AbstractRecommenderComponentBuilder<BaselineSubtractingNormalizer> {
        private RecommenderComponentBuilder<? extends BaselinePredictor> baselineBuilder;
        
        public Builder() {
            this(new ConstantPredictor.Builder());
        }
        
        public Builder(RecommenderComponentBuilder<? extends BaselinePredictor> builder) {
            baselineBuilder = builder;
        }

        public RecommenderComponentBuilder<? extends BaselinePredictor> predictor;
        
        public RecommenderComponentBuilder<? extends BaselinePredictor> getBaselinePredictor() {
            return predictor;
        }
        
        public void setBaselinePredictor(RecommenderComponentBuilder<? extends BaselinePredictor> predictor) {
            baselineBuilder = predictor;
        }

        @Override
        protected BaselineSubtractingNormalizer buildNew(RatingBuildContext context) {
            BaselinePredictor predictor = baselineBuilder.build(context);
            return new BaselineSubtractingNormalizer(predictor);
        }
    }
    
    protected final BaselinePredictor baselinePredictor;
    
    public BaselineSubtractingNormalizer(BaselinePredictor baseline) {
        baselinePredictor = baseline;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.norm.UserRatingVectorNormalizer#normalize(long, org.grouplens.lenskit.data.vector.SparseVector, org.grouplens.lenskit.data.vector.MutableSparseVector)
     */
    @Override
    public void normalize(long userId, SparseVector ratings,
            MutableSparseVector vector) {
        SparseVector base = baselinePredictor.predict(userId, ratings, vector.keySet());
        vector.subtract(base);
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.norm.UserRatingVectorNormalizer#makeTransformation(long, org.grouplens.lenskit.data.vector.SparseVector)
     */
    @Override
    public VectorTransformation makeTransformation(long userId,
            SparseVector ratings) {
        return new Transformation(userId, ratings.immutable());
    }
    
    protected class Transformation implements VectorTransformation {
        private final long userId;
        private final ImmutableSparseVector ratings;
        
        public Transformation(long uid, ImmutableSparseVector r) {
            userId = uid;
            ratings = r;
        }

        @Override
        public void apply(MutableSparseVector vector) {
            SparseVector base = baselinePredictor.predict(userId, ratings, vector.keySet());
            vector.subtract(base);
        }

        @Override
        public void unapply(MutableSparseVector vector) {
            SparseVector base = baselinePredictor.predict(userId, ratings, vector.keySet());
            vector.add(base);
        }
        
    }

}
