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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;

import javax.inject.Inject;

import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * A <tt>RatingPredictor</tt> that implements a weighted Slope One algorithm.
 */
public class WeightedSlopeOneRatingPredictor extends SlopeOneRatingPredictor {
    @Inject
    public WeightedSlopeOneRatingPredictor(DataAccessObject dao, SlopeOneModel model) {
        super(dao, model);
    }

    @Override
    public SparseVector score(UserHistory<? extends Event> history, Collection<Long> items) {
        UserVector user = RatingVectorUserHistorySummarizer.makeRatingVector(history);

        LongSortedSet iset;
        if (items instanceof LongSortedSet) {
            iset = (LongSortedSet) items;
        } else {
            iset = new LongSortedArraySet(items);
        }
        MutableSparseVector preds = new MutableSparseVector(iset, Double.NaN);
        LongArrayList unpreds = new LongArrayList();
        for (long predicteeItem : items) {
            if (!user.containsKey(predicteeItem)) {
                double total = 0;
                int nusers = 0;
                for (long currentItem : user.keySet()) {
                    double currentDev = model.getDeviation(predicteeItem, currentItem);
                    if (!Double.isNaN(currentDev)) {
                        int weight = model.getCoratings(predicteeItem, currentItem);
                        total += (currentDev +user.get(currentItem))* weight;
                        nusers += weight;
                    }
                }
                if (nusers == 0) {
                    unpreds.add(predicteeItem);
                } else {
                    double predValue = total/nusers;
                    predValue = model.getDomain().clampValue(predValue);
                    preds.set(predicteeItem, predValue);
                }
            }
        }
        
        final BaselinePredictor baseline = model.getBaselinePredictor();
        if (baseline != null && !unpreds.isEmpty()) {
            SparseVector basePreds = baseline.predict(user, unpreds);
            preds.set(basePreds);
        }
        
        return preds;
    }
}
