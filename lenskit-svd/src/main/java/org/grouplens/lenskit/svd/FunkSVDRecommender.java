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
/**
 * 
 */
package org.grouplens.lenskit.svd;

import org.grouplens.lenskit.BasketRecommender;
import org.grouplens.lenskit.DynamicRatingPredictor;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RatingRecommender;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.RecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DataAccessObjectManager;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.svd.params.IterationCount;

/**
 * The SVD model used for recommendation and prediction.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class FunkSVDRecommender implements Recommender {
    public final RatingDataAccessObject dao;
	public final SVDRatingPredictor predictor;
	
	public FunkSVDRecommender(RatingDataAccessObject dao, SVDRatingPredictor predictor) {
	    this.dao = dao;
	    this.predictor = predictor;
	}
	
    @Override
    public RatingPredictor getRatingPredictor() {
        return predictor;
    }
    
    @Override
    public DynamicRatingPredictor getDynamicRatingPredictor() {
        return null;
    }

    @Override
    public RatingRecommender getRatingRecommender() {
        return null;
    }

    @Override
    public BasketRecommender getBasketRecommender() {
        return null;
    }

    @Override
    public void close() {
        dao.close();
    }

    @Override
    public RatingDataAccessObject getRatingDataAccessObject() {
        return dao;
    }
    
    public static RecommenderEngine make(DataAccessObjectManager<? extends RatingDataAccessObject> manager, int iterCount) {
        RecommenderEngineFactory factory = new RecommenderEngineFactory();
        factory.setRecommender(FunkSVDRecommender.class);
        factory.bind(IterationCount.class, iterCount);
        return factory.create(manager);
    }
}
