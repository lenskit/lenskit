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
package org.grouplens.lenskit.eval.algorithm;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A trained recommender instance for testing. The recommender has been trained, and
 * a new recommender is opened. If the test data needs to be pre-supplied, it has been.
 * This class is only used for evaluations.
 */
public interface RecommenderInstance {
    /**
     * Get the DAO backing the recommender (the training DAO).
     * @return The training DAO.
     */
    UserEventDAO getUserEventDAO();

    /**
     * Get the recommender's results for the test user.
     * @param uid The user ID.
     * @return The user's recommendation results for measurement.
     */
    TestUser getUserResults(long uid);

    /**
     * Get a user's predictions.
     * @param uid The user.
     * @param testItems The test items.
     * @return The user's predictions.
     */
    SparseVector getPredictions(long uid, LongSet testItems);

    /**
     * Get the user's recommendations.
     *
     * @param uid The user ID.
     * @param testItems The test items.
     * @param n The number of recommendations.
     * @return Recommendations for the user.
     */
    List<ScoredId> getRecommendations(long uid, LongSet testItems, int n);

    /**
     * Get the recommender, if this instance has one.
     * @return The recommender, or {@code null} if this instance doesn't have one.
     */
    @Nullable
    Recommender getRecommender();
}
