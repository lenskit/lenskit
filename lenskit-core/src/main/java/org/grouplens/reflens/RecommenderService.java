/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.reflens;

import javax.annotation.Nonnull;

/**
 * Interface for recommender engines, providing access to specific types of
 * recommender interfaces.
 * 
 * The reason we have this interface returning other interfaces is so that any
 * class that has e.g. a {@link RatingRecommender} knows it can get rating 
 * recommendations without worrying about null values (aside from out-of-domain
 * inputs) or {@link UnsupportedOperationException}s.
 * 
 * You will usually get one of these from a {@link RecommenderServiceManager}.  It will
 * build them with a {@link RecommenderBuilder}.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface RecommenderService {
	/**
	 * Retrieve the rating recommender from this engine.
	 * @return a ratings-based recommender.
	 * @throws IncompatibleRecommenderException if the recommender service does
	 * not support rating-based recommendation.
	 */
	
	@Nonnull RatingRecommender getRatingRecommender();
	/**
	 * Retrieve the rating predictor from this engine.
	 * @return a rating predictor, or <tt>null</tt> if ratings cannot be
	 * predicted.
	 * @throws IncompatibleRecommenderException if the recommender service does
	 * not support rating prediction.
	 */
	@Nonnull RatingPredictor getRatingPredictor();
	/**
	 * Retrieve the basket-based recommender for this engine.
	 * @return a basket recommender, or <tt>null</tt> if basket-based
	 * recommendation is not supported.
	 * @throws IncompatibleRecommenderException if the recommender service does
	 * not support basket-based recommendation.
	 */
	@Nonnull BasketRecommender getBasketRecommender();
}
