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
package org.grouplens.reflens.knn.user;

import org.grouplens.reflens.AbstractRecommenderService;
import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;

import com.google.inject.Inject;

/**
 * Simple user-user recommender that does a brute-force search over the
 * data source for every recommendation.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserUserRecommenderService extends AbstractRecommenderService {
	private final AbstractUserUserRatingRecommender recommender;
	
	@Inject
	UserUserRecommenderService(AbstractUserUserRatingRecommender recommender) {
		this.recommender = recommender;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RecommenderService#getRatingRecommender()
	 */
	@Override
	public RatingRecommender getRatingRecommender() {
		return recommender;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RecommenderService#getRatingPredictor()
	 */
	@Override
	public RatingPredictor getRatingPredictor() {
		return recommender;
	}
}
