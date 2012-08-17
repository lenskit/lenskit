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
package org.grouplens.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A {@code RatingPredictor} that implements a weighted Slope One algorithm.
 */
public class WeightedSlopeOneRatingPredictor extends SlopeOneRatingPredictor {
    @Inject
    public WeightedSlopeOneRatingPredictor(DataAccessObject dao, SlopeOneModel model) {
        super(dao, model);
    }

    @Override
    public void score(@Nonnull UserHistory<? extends Event> history,
                      @Nonnull MutableSparseVector scores) {
        SparseVector ratings = RatingVectorUserHistorySummarizer.makeRatingVector(history);

        int nUnpred = 0;
        for (VectorEntry e : scores.fast(VectorEntry.State.EITHER)) {
            final long predicteeItem = e.getKey();
            if (!ratings.containsKey(predicteeItem)) {
                double total = 0;
                int nusers = 0;
                LongIterator ratingIter = ratings.keySet().iterator();
                while (ratingIter.hasNext()) {
                    long currentItem = ratingIter.nextLong();
                    double currentDev = model.getDeviation(predicteeItem, currentItem);
                    if (!Double.isNaN(currentDev)) {
                        int weight = model.getCoratings(predicteeItem, currentItem);
                        total += (currentDev + ratings.get(currentItem)) * weight;
                        nusers += weight;
                    }
                }
                if (nusers == 0) {
                    nUnpred += 1;
                    scores.clear(e);
                } else {
                    double predValue = total / nusers;
                    predValue = model.getDomain().clampValue(predValue);
                    scores.set(e, predValue);
                }
            }
        }

        final BaselinePredictor baseline = model.getBaselinePredictor();
        if (baseline != null && nUnpred > 0) {
            baseline.predict(history.getUserId(), ratings, scores, false);
        }
    }
}
