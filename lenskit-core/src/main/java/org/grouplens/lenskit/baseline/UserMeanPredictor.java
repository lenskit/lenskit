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
package org.grouplens.lenskit.baseline;

import java.util.Collection;

import org.grouplens.lenskit.AbstractRecommenderComponentBuilder;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rating predictor that returns the user's average rating for all predictions.
 *
 * If the user has no ratings, the global mean is returned.  This is done by
 * actually computing the average offset from the global mean and adding back
 * the global mean for the returned prediction.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserMeanPredictor implements BaselinePredictor {
    private static final Logger logger = LoggerFactory.getLogger(UserMeanPredictor.class);
    /**
     * A builder that creates UserMeanPredictors.
     * 
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     */
    public static class Builder extends AbstractRecommenderComponentBuilder<UserMeanPredictor> {
        private double smoothing = 0;
        
        public void setSmoothing(double smoothing) {
        	this.smoothing = smoothing;
        }
        
        public double getSmoothing() {
        	return smoothing;
        }
    	
    	@Override
        protected UserMeanPredictor buildNew(RatingBuildContext context) {
            logger.debug("Building new user mean predictor");
            logger.debug("smoothing = {}", smoothing);
            double mean = GlobalMeanPredictor.computeMeanRating(context.ratingSnapshot());
            logger.debug("Computed global mean {}", mean);
            return new UserMeanPredictor(mean, smoothing);
        }
    }
    
    private static final long serialVersionUID = 1L;
    private final double globalMean;
    private final double smoothing;

    /**
     * Construct a predictor that computes user means offset by the global mean.
     * @param ratings
     */
    protected UserMeanPredictor(double globalMean, double smoothing) {
        this.globalMean = globalMean;
        this.smoothing = smoothing;
    }

    static double average(SparseVector ratings, double globalMean, double smoothing) {
        if (ratings.isEmpty()) return globalMean;
        return (ratings.sum() + smoothing*globalMean) / (ratings.size() + smoothing);
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
     */
    @Override
    public MutableSparseVector predict(long user, SparseVector ratings,
            Collection<Long> items) {
        double mean = average(ratings, globalMean, smoothing);
        return ConstantPredictor.constantPredictions(items, mean);
    }
}
