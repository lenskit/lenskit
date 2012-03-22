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

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.core.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.RatingVectorHistorySummarizer;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

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
    public SparseVector score(UserHistory<? extends Event> history, Collection<Long> items) {
        UserVector user = RatingVectorHistorySummarizer.makeRatingVector(history);

        LongSortedSet iset;
        if (items instanceof LongSortedSet) {
            iset = (LongSortedSet) items;
        } else {
            iset = new LongSortedArraySet(items);
        }
        
        MutableSparseVector preds = new MutableSparseVector(iset);
        LongArrayList unpreds = new LongArrayList();
        for (long predicteeItem : items) {
            if (!user.containsKey(predicteeItem)) {
                double total = 0;
                int nitems = 0;
                for (long currentItem : user.keySet()) {
                    int nusers = model.getCoratings(predicteeItem, currentItem);
                    if (nusers != 0) {
                        double currentDev = model.getDeviation(predicteeItem, currentItem);
                        total += currentDev + user.get(currentItem);
                        nitems++;
                    }
                }
                if (nitems == 0) {
                    unpreds.add(predicteeItem);
                } else {
                    double predValue = total/nitems;
                    if (predValue > model.getMaxRating()) {
                        predValue = model.getMaxRating();
                    } else if (predValue < model.getMinRating()) {
                        predValue = model.getMinRating();
                    }
                    preds.set(predicteeItem, predValue);
                }
            }
        }
        
        //Use Baseline Predictor if necessary
        final BaselinePredictor baseline = model.getBaselinePredictor();
        if (baseline != null && !unpreds.isEmpty()) {
            SparseVector basePreds = baseline.predict(user, unpreds);
            preds.set(basePreds);
        }
        
        return preds;
    }

    public SlopeOneModel getModel() {
        return model;
    }
}