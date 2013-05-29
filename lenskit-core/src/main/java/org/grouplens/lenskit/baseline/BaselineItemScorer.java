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

import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

/**
 * {@link org.grouplens.lenskit.ItemScorer} that delegates to the baseline predictor. This allows
 * baseline predictors to be used as rating predictors in their own right.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @see BaselinePredictor
 */
public class BaselineItemScorer extends AbstractItemScorer {
    private BaselinePredictor predictor;

    /**
     * Construct a new baseline rating predictor.
     *
     * @param baseline The baseline predictor to use.
     * @param dao      The DAO.
     */
    @Inject
    public BaselineItemScorer(BaselinePredictor baseline, DataAccessObject dao) {
        super(dao);
        predictor = baseline;
    }

    /**
     * Get the baseline predictor under this scorer.
     * @return The baseline predictor.
     */
    public BaselinePredictor getBaseline() {
        return predictor;
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link BaselinePredictor#predict(long, MutableSparseVector)}.
     */
    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        predictor.predict(user, scores);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link BaselinePredictor#predict(long, SparseVector, MutableSparseVector)}.
     */
    @Override
    public void score(@Nonnull UserHistory<? extends Event> profile,
                      @Nonnull MutableSparseVector scores) {
        SparseVector ratings = RatingVectorUserHistorySummarizer.makeRatingVector(profile);
        predictor.predict(profile.getUserId(), ratings, scores);
    }
}
