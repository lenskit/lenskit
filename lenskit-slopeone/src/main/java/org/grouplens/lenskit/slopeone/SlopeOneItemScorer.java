/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;
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
 * An {@link org.grouplens.lenskit.ItemScorer} that implements the Slope One algorithm.
 */
public class SlopeOneItemScorer extends AbstractItemScorer {

    protected final UserEventDAO dao;
    protected SlopeOneModel model;
    protected final PreferenceDomain domain;

    @Inject
    public SlopeOneItemScorer(UserEventDAO dao,
                              SlopeOneModel model,
                              @Nullable PreferenceDomain dom) {
        this.dao = dao;
        this.model = model;
        domain = dom;
    }

    @Override
    public void score(long uid, @Nonnull MutableSparseVector scores) {
        UserHistory<Rating> history = dao.getEventsForUser(uid, Rating.class);
        if (history == null) {
            history = History.forUser(uid);
        }
        SparseVector user = RatingVectorUserHistorySummarizer.makeRatingVector(history);

        for (VectorEntry e : scores.view(VectorEntry.State.EITHER)) {
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
                    double predValue = total / nitems;
                    if (domain != null) {
                        predValue = domain.clampValue(predValue);
                    }
                    scores.set(e, predValue);
                } else {
                    scores.unset(e);
                }
            }
        }
    }

    public SlopeOneModel getModel() {
        return model;
    }
}
