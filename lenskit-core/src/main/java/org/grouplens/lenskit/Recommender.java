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

import org.grouplens.lenskit.data.dao.DataAccessObject;

/**
 * Main entry point for accessing recommender components.  A recommender object
 * is effectively a recommender <i>session</i>: it is a per-thread or per-request
 * object, likely connected to a database connection or persistence session, that
 * needs to be closed when the client code is finished with it.
 * 
 * <p>The various methods in this class return <var>null</var> if the corresponding
 * operation is not supported by the underlying recommender configuration.  This
 * ensures that, if you can actually get an object implementing a particular interface,
 * you are guaranteed to be able to use it.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @see LenskitRecommender
 * @see RecommenderEngine
 */
public interface Recommender {
    /**
     * Get the rating DAO for this recommender session.
     * @return The DAO, or <var>null</var> if this recommender is not connected
     * to a DAO.  All LensKit recommenders are connected to DAOs; recommenders
     * from other frameworks that are adapted to the LensKit API may not be.
     */
    DataAccessObject getRatingDataAccessObject();
    
    /**
     * Get the recommender's rating predictor.
     * @return The rating predictor for this recommender configuration, or
     * <tt>null</tt> if rating prediction is not supported.
     */
    @Nullable
    RatingPredictor getRatingPredictor();
    
    /**
     * Get the recommender's dynamic rating predictor.
     * @return The rating predictor for this recommender configuration, or
     * <tt>null</tt> if dynamic rating prediction is not supported.
     */
    @Nullable
    DynamicRatingPredictor getDynamicRatingPredictor();
    
    /**
     * Get the recommender's item recommender.
     * 
     * @return The item recommender for this recommender configuration, or
     *         <tt>null</tt> if item recommendation is not supported.
     */
    @Nullable
    ItemRecommender getItemRecommender();
    
    /**
     * Get the recommender's rating-based dynamic item recommender.
     * 
     * @return The rating-based dynamic item recommender for this recommender
     *         configuration, or <tt>null</tt> if rating-based dynamic item
     *         recommendation is not supported.
     */
    @Nullable
    DynamicRatingItemRecommender getDynamicItemRecommender();
    
    /**
     * Close the recommender session.  Underlying data connections are released
     * as appropriate.
     * 
     * @see RecommenderEngine#open(DataAccessObject, boolean)
     */
    void close();
    
    
}