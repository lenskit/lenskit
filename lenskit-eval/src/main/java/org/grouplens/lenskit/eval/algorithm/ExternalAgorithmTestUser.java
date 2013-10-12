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

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelector;
import org.grouplens.lenskit.eval.traintest.AbstractTestUser;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

import java.util.List;

/**
 * External algorithm implementation of TestUser.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ExternalAgorithmTestUser extends AbstractTestUser {
    private final UserEventDAO trainDAO;
    private final UserHistory<Event> userHistory;
    private final SparseVector predictions;

    public ExternalAgorithmTestUser(UserEventDAO train, UserHistory<Event> uh, SparseVector preds) {
        trainDAO = train;
        userHistory = uh;
        predictions = preds;
    }

    @Override
    public UserHistory<Event> getTrainHistory() {
        return trainDAO.getEventsForUser(getUserId());
    }

    @Override
    public UserHistory<Event> getTestHistory() {
        return userHistory;
    }

    @Override
    public SparseVector getPredictions() {
        return predictions;
    }

    @Override
    public List<ScoredId> getRecommendations(int n, ItemSelector candSel, ItemSelector exclSel) {
        throw new UnsupportedOperationException("external algorithms are predict-only");
    }

    @Override
    public Recommender getRecommender() {
        throw new UnsupportedOperationException("external algorithms do not have recommenders");
    }
}
