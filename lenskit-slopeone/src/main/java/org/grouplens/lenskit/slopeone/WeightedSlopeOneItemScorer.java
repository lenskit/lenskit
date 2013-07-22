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
package org.grouplens.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * An {@link org.grouplens.lenskit.ItemScorer} that implements a weighted Slope One algorithm.
 */
public class WeightedSlopeOneItemScorer extends SlopeOneItemScorer {
    @Inject
    public WeightedSlopeOneItemScorer(UserEventDAO dao, SlopeOneModel model,
                                      @Nullable PreferenceDomain dom) {
        super(dao, model, dom);
    }

    @Override
    public void score(long uid, @Nonnull MutableSparseVector scores) {
        UserHistory<Rating> history = dao.getEventsForUser(uid, Rating.class);
        SparseVector ratings = RatingVectorUserHistorySummarizer.makeRatingVector(history);

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
                    scores.unset(e);
                } else {
                    double predValue = total / nusers;
                    if (domain != null) {
                        predValue = domain.clampValue(predValue);
                    }
                    scores.set(e, predValue);
                }
            }
        }
    }
}
