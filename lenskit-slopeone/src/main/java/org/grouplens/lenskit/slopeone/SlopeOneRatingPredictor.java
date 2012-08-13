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
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.core.AbstractItemScorer;
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
 * A <tt>RatingPredictor<tt> that implements the Slope One algorithm.
 */
public class SlopeOneRatingPredictor extends AbstractItemScorer implements RatingPredictor {

    protected SlopeOneModel model;

    @Inject
    public SlopeOneRatingPredictor(DataAccessObject dao, SlopeOneModel model) {
        super(dao);
        this.model = model;
    }

    @Override
    public void score(@Nonnull UserHistory<? extends Event> history,
                      @Nonnull MutableSparseVector scores) {
        SparseVector user = RatingVectorUserHistorySummarizer.makeRatingVector(history);

        int nUnpred = 0;
        for (VectorEntry e: scores.fast(VectorEntry.State.EITHER)) {
            final long predicteeItem = e.getKey();
        	if (!user.containsKey(predicteeItem)) {
                double total = 0;
                int nitems = 0;
                LongIterator ratingIter = user.keySet().iterator();
                while (ratingIter.hasNext()) {
                    long currentItem = ratingIter.nextLong();
                	int nusers = model.getCoratings(predicteeItem, currentItem);
                    if (nusers != 0) {
                        double currentDev = model.getDeviation(predicteeItem, currentItem);
                        total += currentDev + user.get(currentItem);
                        nitems++;
                    }
                }
                if (nitems != 0) {
                    double predValue = total/nitems;
                    predValue = model.getDomain().clampValue(predValue);
                    scores.set(e, predValue);
                } else {
                    nUnpred += 1;
                    scores.clear(e);
                }
            }
        }
        
        //Use Baseline Predictor if necessary
        final BaselinePredictor baseline = model.getBaselinePredictor();
        if (baseline != null && nUnpred > 0) {
            baseline.predict(history.getUserId(), user, scores, false);
        }
    }

    public SlopeOneModel getModel() {
        return model;
    }
}
