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
package org.grouplens.lenskit.eval.traintest;

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelector;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Expose recommender data to evaluate recommendations and predictions for a single user.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface TestUser {
    /**
     * Get the ID of this user.
     *
     * @return The user's ID.
     */
    long getUserId();

    /**
     * Return this user's training history.
     *
     * @return The history of the user from the training/query set.
     */
    UserHistory<Event> getTrainHistory();

    /**
     * Return this user's test history.
     *
     * @return The history of the user from the test set.
     */
    UserHistory<Event> getTestHistory();

    /**
     * Get the user's test ratings.
     *
     * Summarizes the user's ratings from the history.
     *
     * @return The user's ratings for the test items.
     */
    SparseVector getTestRatings();

    /**
     * Get the user's predictions.
     *
     * @return The predictions of the user's preference for items, or {@code null} if the algorithm
     *         does not support rating prediction.
     */
    @Nullable
    SparseVector getPredictions();

    /**
     * Get the user's recommendations.
     *
     * @param n       The number of recommendations to generate.
     * @param candSel The candidate selector.
     * @param exclSel The exclude selector.
     * @return Some recommendations for the user, or {@code null} if the recommender does not
     *         support recommendation.
     * @see org.grouplens.lenskit.ItemRecommender#recommend(long, int, java.util.Set,
     *      java.util.Set)
     */
    @Nullable
    List<ScoredId> getRecommendations(int n, ItemSelector candSel, ItemSelector exclSel);

    /**
     * Get the recommender
     * @return The recommender.
     */
    @Nullable
    Recommender getRecommender();
}
