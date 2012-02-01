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

import java.io.Serializable;
import java.util.Collection;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Rating predictor that operates on sparse vectors and guarantees 100% coverage
 * of items. Because BaselinePredictors are often part of a "model" used by a
 * recommender algorithm, all BaselinePredictors are required to be Serializable
 * so that they can be easily written to or read from a file.
 *
 * <p>
 * Note that this class does not implement the {@link RatingPredictor} interface
 * - this is to allow it to operate free of the DAO. If you want to use a
 * baseline scorer as a {@link RatingPredictor}, see
 * {@link BaselineRatingPredictor}.
 *
 * @author Michael Ludwig
 * @see BaselineRatingPredictor
 */
public interface BaselinePredictor extends Serializable {
    /**
     * Predict method that returns mutable sparse vectors.
     * @see RatingPredictor#score(UserHistory, Collection)
     */
    MutableSparseVector predict(UserVector ratings, Collection<Long> items);
    
    MutableSparseVector predict(long item, Collection<Long> items);
}
