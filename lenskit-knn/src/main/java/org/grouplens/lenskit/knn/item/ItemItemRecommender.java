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
package org.grouplens.lenskit.knn.item;

import org.grouplens.lenskit.BasketRecommender;
import org.grouplens.lenskit.DynamicRatingPredictor;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RatingRecommender;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.RecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DataAccessObjectManager;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.knn.SimilarityMatrixAccumulatorFactory;
import org.grouplens.lenskit.knn.TruncatingSimilarityMatrixAccumulator;

public class ItemItemRecommender implements Recommender {
    private final RatingDataAccessObject dao;
    private final ItemItemRatingPredictor predictor;
    private final ItemItemRatingRecommender recommender;
    
    public ItemItemRecommender(RatingDataAccessObject dao, ItemItemRatingPredictor predictor,
                               ItemItemRatingRecommender recommender) {
        this.dao = dao;
        this.predictor = predictor;
        this.recommender = recommender;
    }
    
    @Override
    public RatingPredictor getRatingPredictor() {
        return predictor;
    }

    @Override
    public DynamicRatingPredictor getDynamicRatingPredictor() {
        return predictor;
    }

    @Override
    public RatingRecommender getRatingRecommender() {
        return recommender;
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
    
    public static RecommenderEngine make(DataAccessObjectManager<? extends RatingDataAccessObject> manager) {
        RecommenderEngineFactory factory = new RecommenderEngineFactory();
        factory.setRecommender(ItemItemRecommender.class);
        factory.bindDefault(SimilarityMatrixAccumulatorFactory.class, 
                            TruncatingSimilarityMatrixAccumulator.Factory.class);
        return factory.create(manager);
    }
}
