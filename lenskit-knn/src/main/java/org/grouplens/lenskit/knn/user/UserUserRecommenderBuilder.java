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
import org.grouplens.lenskit.data.context.RatingBuildContext;

/**
 * UserUserRecommenderBuilder is a RecommenderComponentBuilder that is used to
 * provide UserUserRecommenders.
 * 
 * @author Michael Ludwig
 */
public class UserUserRecommenderBuilder extends AbstractRecommenderComponentBuilder<UserUserRecommender> {
    private RecommenderComponentBuilder<? extends NeighborhoodFinder> neighborhoodBuilder;
    
    public UserUserRecommenderBuilder() {
        neighborhoodBuilder = new SimpleNeighborhoodFinder.Builder();
    }
    
    public void setNeighborhoodFinder(RecommenderComponentBuilder<? extends NeighborhoodFinder> neighborhood) {
        neighborhoodBuilder = neighborhood;
    }
    
    @Override
    protected UserUserRecommender buildNew(RatingBuildContext context) {
        NeighborhoodFinder n = neighborhoodBuilder.build(context);
        UserUserRatingPredictor pred = new UserUserRatingPredictor(context.getDAO(), n);
        UserUserRatingRecommender rec = new UserUserRatingRecommender(pred);
        return new UserUserRecommender(pred, rec);
    }
}
