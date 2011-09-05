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

import org.grouplens.lenskit.AbstractItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.RatingVectorHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.data.vector.UserVector;

/**
 * {@link RatingPredictor} that delegates to the baseline predictor. This allows
 * baseline predictors to be used as rating predictors in their own right.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @see BaselinePredictor
 */
public class BaselineRatingPredictor extends AbstractItemScorer {
    private BaselinePredictor predictor;

    /**
     * Construct a new baseline rating predictor.
     * @param baseline The scorer to delegate to
     * @param dao The DAO.
     */
    public BaselineRatingPredictor(BaselinePredictor baseline, DataAccessObject dao) {
        super(dao);
        predictor = baseline;
    }

    @Override
    public SparseVector score(UserHistory<? extends Event> profile, Collection<Long> items) {
        UserVector ratings = RatingVectorHistorySummarizer.makeRatingVector(profile);
        return predictor.predict(ratings, items);
    }
}
