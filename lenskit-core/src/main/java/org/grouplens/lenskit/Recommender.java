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
package org.grouplens.lenskit;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

/**
 * Main entry point for accessing recommender components.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface Recommender {
    /**
     * Get a particular component from the recommender session.  Generally
     * you want to use one of the type-specific getters; this method only exists
     * for specialized applications which need deep access to the recommender
     * components.
     * @param <T>
     * @param cls
     * @return
     */
    <T> T getComponent(Class<T> cls);
    
    @Nullable
    RatingPredictor getRatingPredictor();
    
    @Nullable
    DynamicRatingPredictor getDynamicRatingPredictor();
    
    @Nullable
    DynamicRatingItemRecommender getDynamicItemRecommender();
    
    @Nullable
    BasketRecommender getBasketRecommender();
    
    /**
     * Get the rating DAO for this recommender session.
     * @return
     */
    RatingDataAccessObject getRatingDataAccessObject();
    
    @Nullable
    ItemRecommender getItemRecommender();
    
    /**
     * Close the recommender session.  Underlying data connections are released
     * as appropriate.
     */
    void close();
    
    
}