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

import javax.inject.Inject;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.core.ScoreBasedItemRecommender;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;

/**
 * A <tt>RatingRecommender</tt> that uses the Slope One algorithm.
 */
public class SlopeOneRecommender extends ScoreBasedItemRecommender {
    private SlopeOneRatingPredictor predictor;

    /**
     * Construct a new recommender from a scorer.
     * @param predictor The predictor to use.
     */
    @Inject
    public SlopeOneRecommender(DataAccessObject dao, SlopeOneRatingPredictor predictor) {
        super(dao, predictor);
        this.predictor = predictor;
    }

    @Override
    protected LongSet getPredictableItems(UserHistory<? extends Event> user) {
        if (predictor.getModel().getBaselinePredictor() != null) {
            return predictor.getModel().getItemUniverse();
        } else {
            LongSet predictable = new LongOpenHashSet();
            LongIterator iter1 = predictor.getModel().getItemUniverse().iterator();
            while (iter1.hasNext()) {
                long id1 = iter1.nextLong();
            	LongIterator iter2 = user.filter(Rating.class).itemSet().iterator();
                int nusers = 0;
                while (iter2.hasNext() && nusers == 0) {
                    long id2 = iter2.nextLong();
                	nusers += predictor.getModel().getCoratings(id1, id2);
                }
                if (nusers > 0) {
                    predictable.add(id1);
                }
            }
            return predictable;
        }
    }
}
