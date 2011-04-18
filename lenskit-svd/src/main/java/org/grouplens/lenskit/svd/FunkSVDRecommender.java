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
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.util.DoubleFunction;

/**
 * The SVD model used for recommendation and prediction.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class FunkSVDRecommender implements Recommender {
    public final RatingDataAccessObject dao;
	public final int featureCount;
	public final double itemFeatures[][];
	public final double singularValues[];
	public final DoubleFunction clampingFunction;
	
	public final Index itemIndex;
	public final BaselinePredictor baseline;
	
	public FunkSVDRecommender(RatingDataAccessObject dao,
	        int nfeatures, double[][] ifeats, double[] svals,
	        DoubleFunction clamp, Index iidx,
	        BaselinePredictor baseline) {
	    this.dao = dao;
		featureCount = nfeatures;
		itemFeatures = ifeats;
		singularValues = svals;
		clampingFunction = clamp;
		itemIndex = iidx;
		this.baseline = baseline;
	}
	
	public double getItemFeatureValue(int item, int feature) {
	    return itemFeatures[feature][item];
	}
	
	public int getItemIndex(long item) {
	    return itemIndex.getIndex(item);
	}

    @Override
    public RatingPredictor getRatingPredictor() {
        return new SVDRatingPredictor(this);
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
}
