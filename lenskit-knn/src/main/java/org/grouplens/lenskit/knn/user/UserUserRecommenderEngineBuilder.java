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
package org.grouplens.lenskit.knn.user;

import org.grouplens.lenskit.AbstractRecommenderComponentBuilder;
import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.norm.IdentityUserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;

/**
 * UserUserRecommenderEngineBuilder is a RecommenderComponentBuilder that is used to
 * provide UserUserRecommenders.
 * 
 * @author Michael Ludwig
 */
public class UserUserRecommenderEngineBuilder extends AbstractRecommenderComponentBuilder<UserUserRecommenderEngine> {
    private RecommenderComponentBuilder<? extends NeighborhoodFinder> neighborhoodBuilder;
    private RecommenderComponentBuilder<? extends BaselinePredictor> baselineBuilder;
    private RecommenderComponentBuilder<? extends UserRatingVectorNormalizer> normalizerBuilder;
    
    public UserUserRecommenderEngineBuilder() {
        neighborhoodBuilder = new SimpleNeighborhoodFinder.Builder();
        normalizerBuilder = new IdentityUserRatingVectorNormalizer.Builder();
    }
    
    public RecommenderComponentBuilder<? extends NeighborhoodFinder> getNeighborhoodFinder() {
        return neighborhoodBuilder;
    }
    
    public void setNeighborhoodFinder(RecommenderComponentBuilder<? extends NeighborhoodFinder> neighborhood) {
        neighborhoodBuilder = neighborhood;
    }
    
    /**
     * Get the baseline predictor builder. 
     * @return The builder for the baseline predictor.
     */
    public RecommenderComponentBuilder<? extends BaselinePredictor> getBaseline() {
        return baselineBuilder;
    }
    
    /**
     * Set the baseline predictor to use for the recommender & predictor.
     * @param baseline The baseline predictor builder.
     * @review Do we want to allow configuration as to whether to use the baseline
     * in recommendation?
     */
    public void setBaseline(RecommenderComponentBuilder<? extends BaselinePredictor> baseline) {
        baselineBuilder = baseline;
    }
    
    /**
     * Set the normalizer builder.
     * @param normalizerBuilder The normalizer builder instance.
     */
    public void setNormalizer(RecommenderComponentBuilder<? extends UserRatingVectorNormalizer> normalizerBuilder) {
        this.normalizerBuilder = normalizerBuilder;
    }

    /**
     * Get the normalizer builder.
     * @return The normalizer builder.
     */
    public RecommenderComponentBuilder<? extends UserRatingVectorNormalizer> getNormalizer() {
        return normalizerBuilder;
    }

    @Override
    protected UserUserRecommenderEngine buildNew(RatingBuildContext context) {
        NeighborhoodFinder n = neighborhoodBuilder.build(context);
        UserRatingVectorNormalizer norm = null;
        if (normalizerBuilder != null)
            norm = normalizerBuilder.build(context);
        
        // Build the baseline predictor
        BaselinePredictor baseline = null;
        if (baselineBuilder != null)
            baseline = baselineBuilder.build(context);
        
        // Create the predictor and recommender
        UserUserRatingPredictor pred =
            new UserUserRatingPredictor(context.getDAO(), n, norm, baseline);
        UserUserRatingRecommender rec = new UserUserRatingRecommender(pred);
        return new UserUserRecommenderEngine(pred, rec);
    }
}
