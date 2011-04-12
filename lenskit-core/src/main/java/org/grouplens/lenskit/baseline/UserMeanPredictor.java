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

import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

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
    private static final long serialVersionUID = 1L;

    private final double globalMean;

    /**
     * Construct a predictor that does not do global mean offsetting.
     */
    public UserMeanPredictor() {
        globalMean = 0;
    }

    /**
     * Construct a predictor that computes user means offset by the global mean.
     * @param ratings
     */
    public UserMeanPredictor(RatingDataAccessObject ratings) {
        globalMean = GlobalMeanPredictor.computeMeanRating(ratings.getRatings());
    }

    static double average(SparseVector ratings, double offset) {
        if (ratings.isEmpty()) return 0;

        double total = ratings.sum();
        total -= ratings.size() * offset;
        return total / ratings.size();
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
     */
    @Override
    public MutableSparseVector predict(long user, SparseVector ratings,
            Collection<Long> items) {
        double mean = average(ratings, globalMean) + globalMean;
        return ConstantPredictor.constantPredictions(items, mean);
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RatingPredictor#predict(long, java.util.Map, long)
     */
    @Override
    public ScoredId predict(long user, SparseVector ratings, long item) {
        return new ScoredId(item, average(ratings, globalMean) + globalMean);
    }
}
