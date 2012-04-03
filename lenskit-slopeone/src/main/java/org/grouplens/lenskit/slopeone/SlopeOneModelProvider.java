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

import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.inject.annotation.Transient;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.params.Damping;
import org.grouplens.lenskit.params.MaxRating;
import org.grouplens.lenskit.params.MinRating;
import org.grouplens.lenskit.params.NormalizedSnapshot;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Pre-computes the deviations and number of mutual rating users for every pair
 *  of items and stores the results in a <tt>DeviationMatrix</tt> and
 *  <tt>CoratingMatrix</tt>. These matrices are later used by a
 *  <tt>SlopeOneRatingPredictor</tt>.
 */
public class SlopeOneModelProvider implements Provider<SlopeOneModel> {
    private final SlopeOneModelDataAccumulator accumulator;
    
    private final BaselinePredictor baseline;
    private final double minRating;
    private final double maxRating;
    
    private final RatingSnapshot snapshot;
    
    @Inject
    public SlopeOneModelProvider(@Transient RatingSnapshot snapshot,
                                // FIXME: can this be nullable? Why have two snapshots?
                                @Transient @NormalizedSnapshot RatingSnapshot normalized,
                                BaselinePredictor predictor,
                                @MinRating double min,
                                @MaxRating double max,
                                @Damping double damping) {
        if (normalized != null) {
            this.snapshot = normalized;
        } else {
            this.snapshot = snapshot;
        }
        
        minRating = min;
        maxRating = max;
        baseline = predictor;
        accumulator = new SlopeOneModelDataAccumulator(damping, this.snapshot);
    }

    /**
     * Constructs a {@link SlopeOneModel} and the necessary matrices from
     * a {@link RatingSnapshot}.
     */
    @Override
    public SlopeOneModel get() {
        for (long currentUser : snapshot.getUserIds()) {
            SparseVector ratings = snapshot.userRatingVector(currentUser);
            LongIterator iter = ratings.keySet().iterator();
            while (iter.hasNext()) {
                long item1 = iter.next();
                LongIterator iter2 = ratings.keySet().tailSet(item1).iterator();
                if (iter2.hasNext()) iter2.next();
                while (iter2.hasNext()) {
                    long item2 = iter2.next();
                    accumulator.putRatingPair(item1, ratings.get(item1), item2, ratings.get(item2));
                }
            }
        }
        LongSortedArraySet items = new LongSortedArraySet(snapshot.getItemIds());
        return new SlopeOneModel(accumulator.buildCoratingMatrix(), accumulator.buildDeviationMatrix(), 
                                 baseline, items, minRating, maxRating);
    }
}