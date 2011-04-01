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

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RatingRecommender;
import org.grouplens.lenskit.RecommenderModule;
import org.grouplens.lenskit.knn.NeighborhoodRecommenderModule;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserRecommenderModule extends RecommenderModule {
    public final NeighborhoodRecommenderModule knn = new NeighborhoodRecommenderModule();
    protected Class<? extends NeighborhoodFinder> neighborhoodFinder;

    @Override
    protected void configure() {
        super.configure();
        install(knn);
        bind(NeighborhoodFinder.class).to(neighborhoodFinder);
        bind(RatingPredictor.class).to(UserUserRatingRecommender.class);
        bind(RatingRecommender.class).to(UserUserRatingRecommender.class);
    }

    /**
     * @return the neighborhoodFinder
     */
    public Class<? extends NeighborhoodFinder> getNeighborhoodFinder() {
        return neighborhoodFinder;
    }

    /**
     * @param neighborhoodFinder the neighborhoodFinder to set
     */
    public void setNeighborhoodFinder(Class<? extends NeighborhoodFinder> impl) {
        this.neighborhoodFinder = impl;
    }
    
    @Override
    public void setName(String name) {
        super.setName(name);
        knn.setName(name);
    }
}
