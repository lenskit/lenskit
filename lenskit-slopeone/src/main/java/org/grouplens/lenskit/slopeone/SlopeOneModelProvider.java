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
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.params.Damping;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Pre-computes the deviations and number of mutual rating users for every pair
 *  of items and stores the results in a <tt>DeviationMatrix</tt> and
 *  <tt>CoratingMatrix</tt>. These matrices are later used by a
 *  <tt>SlopeOneRatingPredictor</tt>.
 */
public class SlopeOneModelProvider implements Provider<SlopeOneModel> {
    private final SlopeOneModelDataAccumulator accumulator;
    
    private final BaselinePredictor baseline;
    private final PreferenceDomain domain;
    
    private final PreferenceSnapshot snapshot;
    private UserVectorNormalizer normalizer;

    @Inject
    public SlopeOneModelProvider(@Transient @Nonnull PreferenceSnapshot snap,
                                 @Transient @Nonnull UserVectorNormalizer norm,
                                 @Nullable BaselinePredictor predictor,
                                 @Nonnull PreferenceDomain dom,
                                 @Damping double damping) {
        snapshot = snap;
        normalizer = norm;

        domain = dom;
        baseline = predictor;
        accumulator = new SlopeOneModelDataAccumulator(damping, this.snapshot);
    }

    /**
     * Constructs a {@link SlopeOneModel} and the necessary matrices from
     * a {@link org.grouplens.lenskit.data.snapshot.PreferenceSnapshot}.
     */
    @Override
    public SlopeOneModel get() {
        LongIterator userIter = snapshot.getUserIds().iterator();
    	while (userIter.hasNext()) {
            long u = userIter.nextLong();
    		SparseVector ratings = snapshot.userRatingVector(u);
            SparseVector normed = normalizer.normalize(u, ratings, null);
            LongIterator iter = normed.keySet().iterator();
            while (iter.hasNext()) {
                long item1 = iter.nextLong();
                LongIterator iter2 = normed.keySet().tailSet(item1).iterator();
                if (iter2.hasNext()) {
                    iter2.nextLong();
                }
                while (iter2.hasNext()) {
                    long item2 = iter2.nextLong();
                    accumulator.putRatingPair(item1, normed.get(item1), item2, normed.get(item2));
                }
            }
        }
        return new SlopeOneModel(accumulator.buildCoratingMatrix(), accumulator.buildDeviationMatrix(), 
                                 baseline, snapshot.itemIndex(), domain);
    }
}
