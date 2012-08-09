/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.core.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * {@link RatingPredictor} that delegates to the baseline predictor. This allows
 * baseline predictors to be used as rating predictors in their own right.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @see BaselinePredictor
 */
public class BaselineRatingPredictor extends AbstractItemScorer implements RatingPredictor {
    private BaselinePredictor predictor;

    /**
     * Construct a new baseline rating predictor.
     * 
     * @param baseline The baseline predictor to use.
     * @param dao The DAO.
     */
    @Inject
    public BaselineRatingPredictor(BaselinePredictor baseline, DataAccessObject dao) {
        super(dao);
        predictor = baseline;
    }

    /**
     * Delegate to {@link BaselinePredictor#predict(long, SparseVector, Collection)}.
     */
    @Nonnull
    @Override
    public SparseVector score(UserHistory<? extends Event> profile, Collection<Long> items) {
        SparseVector ratings = RatingVectorUserHistorySummarizer.makeRatingVector(profile);
        return predictor.predict(profile.getUserId(), ratings, items);
    }
}
