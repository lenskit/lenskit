package org.grouplens.lenskit;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

public interface Recommender {
    @Nullable
    public RatingPredictor getRatingPredictor();
    
    @Nullable
    public DynamicRatingPredictor getDynamicRatingPredictor();
    
    @Nullable
    public RatingRecommender getRatingRecommender();
    
    @Nullable
    public BasketRecommender getBasketRecommender();
    
    public void close();
    
    public RatingDataAccessObject getRatingDataAccessObject();
}
