/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import java.io.Serializable;
import java.util.Collection;

/**
 * Rating predictor that operates on sparse vectors and guarantees 100% coverage
 * of items. Because BaselinePredictors are often part of a "model" used by a
 * recommender algorithm, all BaselinePredictors are required to be Serializable
 * so that they can be easily written to or read from a file.
 *
 * <p>
 * Note that this class does not implement the {@link RatingPredictor} interface.
 * If you want to use a baseline scorer as a {@link RatingPredictor}, see
 * {@link BaselineItemScorer}.
 * <p>
 * This class is also misnamed; it generates general item scores, not just rating
 * predictions.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @see BaselineItemScorer
 */
public interface BaselinePredictor extends Serializable {
    /**
     * Generate baseline predictions for several items into a new vector.
     *
     * @param user    The user ID.
     * @param items   The items to score.
     * @return A new sparse vector containing the baseline predictions for {@code items}.
     */
    MutableSparseVector predict(long user, Collection<Long> items);

    /**
     * Predict method that scores into an existing mutable sparse vector.
     *
     * @param user    The user ID.
     * @param output  The output vector. All items in the key domain are scored.
     * @see org.grouplens.lenskit.ItemScorer#score(long, MutableSparseVector)
     */
    void predict(long user, MutableSparseVector output);

    /**
     * Predict method that scores into an existing mutable sparse vector.
     *
     * @param user       The user ID.
     * @param output     The output vector. All items in the key domain are scored
     * @param predictSet If {@code true}, predict all items; otherwise, only predict
     *                   items that are not set..
     * @see org.grouplens.lenskit.ItemScorer#score(long, MutableSparseVector)
     */
    void predict(long user, MutableSparseVector output, boolean predictSet);

    /**
     * Generate baseline predictions for several items into a new vector.
     *
     * @param user    The user ID.
     * @param ratings The user's ratings.
     * @param items   The items to score.
     * @return A new sparse vector containing the baseline predictions for {@code items}.
     */
    MutableSparseVector predict(long user, SparseVector ratings, Collection<Long> items);

    /**
     * Predict method that scores into an existing mutable sparse vector.
     *
     * @param user    The user ID.
     * @param ratings The user's ratings.
     * @param output  The output vector. All items in the key domain are scored.
     * @see RatingPredictor#score(UserHistory, MutableSparseVector)
     */
    void predict(long user, SparseVector ratings, MutableSparseVector output);

    /**
     * Predict method that scores into an existing mutable sparse vector.
     *
     * @param user       The user ID.
     * @param ratings    The user's ratings.
     * @param output     The output vector. All items in the key domain are scored
     * @param predictSet If {@code true}, predict all items; otherwise, only predict
     *                   items that are not set..
     * @see RatingPredictor#score(UserHistory, MutableSparseVector)
     */
    void predict(long user, SparseVector ratings, MutableSparseVector output,
                 boolean predictSet);
}
