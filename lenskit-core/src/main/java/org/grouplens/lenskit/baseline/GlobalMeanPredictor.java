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

import java.util.Iterator;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.params.meta.Built;

/**
 * Rating predictor that predicts the global mean rating for all items.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
@Built
public class GlobalMeanPredictor extends ConstantPredictor {
    /**
     * A default builder used to create GlobalMeanPredictors.
     * 
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     */
    public static class Builder extends RecommenderComponentBuilder<GlobalMeanPredictor> {
        @Override
        public GlobalMeanPredictor build() {
            double avg = computeMeanRating(snapshot.getRatings().fastIterator());
            return new GlobalMeanPredictor(avg);
        }
    }
    
    private static final long serialVersionUID = 1L;

    /**
     * Construct a new global mean predictor where it is assumed
     * that the given value is the global mean.
     * @param mean
     */
    public GlobalMeanPredictor(double mean) {
        super(mean);
    }
    
    /**
     * Utility method to compute the mean or average of the rating values
     * contained in the given collection of ratings.
     * 
     * @param ratings
     * @return
     */
    public static double computeMeanRating(Iterator<? extends Rating> ratings) {
        double total = 0;
        long count = 0;
        
        while(ratings.hasNext()) {
            Rating r = ratings.next();
            total += r.getRating();
            count += 1;
        }
        
        double avg = 0;
        if (count > 0)
            avg = total / count;
        
        return avg;
    }
}
