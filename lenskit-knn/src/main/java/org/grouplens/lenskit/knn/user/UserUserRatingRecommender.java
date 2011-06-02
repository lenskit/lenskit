/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.grouplens.lenskit.AbstractRatingRecommender;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * A recommender and predictor using user-user collaborative filtering.
 * Neighbor ratings are aggregated using weighted averaging. 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserUserRatingRecommender extends AbstractRatingRecommender {
    protected final UserUserRatingPredictor predictor;

    public UserUserRatingRecommender(RatingDataAccessObject dao, UserUserRatingPredictor pred) {
        super(dao);
        predictor = pred;
    }

    @Override
    public List<ScoredId> recommend(long user, SparseVector ratings, int n, LongSet candidates, LongSet exclude) {
        // TODO Share this code with the item-item code
        // FIXME Make this support null candidate sets and exclude
        SparseVector predictions = predictor.predict(user, ratings, candidates);
        PriorityQueue<ScoredId> queue = new PriorityQueue<ScoredId>(predictions.size());
        for (Long2DoubleMap.Entry pred: predictions.fast()) {
            final double v = pred.getDoubleValue();
            final long item = pred.getLongKey();
            if (!Double.isNaN(v) && !exclude.contains(item)) {
                queue.add(new ScoredId(item, v));
            }
        }

        ArrayList<ScoredId> finalPredictions =
            new ArrayList<ScoredId>(n >= 0 ? n : queue.size());
        for (int i = 0; !queue.isEmpty() && (n < 0 || i < n); i++) {
            finalPredictions.add(queue.poll());
        }

        return finalPredictions;
    }
}
