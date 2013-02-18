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
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vectors.SparseVector;

import java.io.Closeable;

/**
 * A trained recommender instance for testing. The recommender has been trained, and
 * a new recommender is opened. If the test data needs to be pre-supplied, it has been.
 * This class is only used for evaluations.
 */
public interface RecommenderInstance extends Closeable {
    /**
     * Get the DAO backing the recommender (the training DAO).
     * @return The training DAO.
     */
    DataAccessObject getDAO();

    /**
     * Get a user's predictions.
     * @param uid The user.
     * @param testItems The test items.
     * @return The user's predictions.
     */
    SparseVector getPredictions(long uid, LongSet testItems);

    /**
     * Get the user's recommendations.
     * @param uid The user ID.
     * @param testItems The test items.
     * @param n The number of recommendations.
     * @return Recommendations for the user.
     */
    ScoredLongList getRecommendations(long uid, LongSet testItems, int n);

    @Override
    void close();
}
