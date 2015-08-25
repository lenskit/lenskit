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
package org.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An {@link org.grouplens.lenskit.ItemScorer} that implements a weighted Slope One algorithm.
 */
public class WeightedSlopeOneItemScorer extends SlopeOneItemScorer {
    @Inject
    public WeightedSlopeOneItemScorer(UserEventDAO dao, SlopeOneModel model,
                                      @Nullable PreferenceDomain dom) {
        super(dao, model, dom);
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        UserHistory<Rating> history = dao.getEventsForUser(user, Rating.class);
        if (history == null) {
            history = History.forUser(user);
        }
        SparseVector userVector = RatingVectorUserHistorySummarizer.makeRatingVector(history);

        List<Result> results = new ArrayList<>();
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long predicteeItem = iter.nextLong();
            if (!userVector.containsKey(predicteeItem)) {
                double total = 0;
                int nitems = 0;
                for (VectorEntry e: userVector) {
                    long currentItem = e.getKey();
                    double currentDev = model.getDeviation(predicteeItem, currentItem);
                    if (!Double.isNaN(currentDev)) {
                        int weight = model.getCoratings(predicteeItem, currentItem);
                        total += (currentDev + e.getValue()) * weight;
                        nitems += weight;
                    }
                }
                if (nitems != 0) {
                    double predValue = total / nitems;
                    if (domain != null) {
                        predValue = domain.clampValue(predValue);
                    }
                    results.add(Results.create(predicteeItem, predValue));
                }
            }
        }
        return Results.newResultMap(results);
    }
}
