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
package org.grouplens.lenskit.mf.funksvd;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Rating estimates used while training the predictor.  An estimator can be constructed
 * using {@link FunkSVDUpdateRule#makeEstimator(PreferenceSnapshot)}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public final class TrainingEstimator {
    private final FastCollection<IndexedPreference> ratings;
    private final ClampingFunction clamp;
    private final double[] estimates;

    /**
     * Initialize the training estimator.
     *
     * @param snap     The preference snapshot.
     * @param baseline The baseline predictor.
     * @param cf       The clamping function.
     */
    public TrainingEstimator(PreferenceSnapshot snap, ItemScorer baseline, ClampingFunction cf) {
        ratings = snap.getRatings();
        clamp = cf;
        estimates = new double[ratings.size()];

        final LongCollection userIds = snap.getUserIds();
        LongIterator userIter = userIds.iterator();
        while (userIter.hasNext()) {
            long uid = userIter.nextLong();
            SparseVector rvector = snap.userRatingVector(uid);
            MutableSparseVector blpreds = MutableSparseVector.create(rvector.keySet());
            baseline.score(uid, blpreds);

            for (IndexedPreference r : CollectionUtils.fast(snap.getUserRatings(uid))) {
                estimates[r.getIndex()] = blpreds.get(r.getItemId());
            }
        }
    }

    /**
     * Get the estimate for a preference.
     * @param pref The preference.
     * @return The estimate.
     */
    public double get(IndexedPreference pref) {
        return estimates[pref.getIndex()];
    }

    /**
     * Update the current estimates with trained values for a new feature.
     * @param ufvs The user feature values.
     * @param ifvs The item feature values.
     */
    public void update(double[] ufvs, double[] ifvs) {
        for (IndexedPreference r : CollectionUtils.fast(ratings)) {
            double est = estimates[r.getIndex()];
            double offset = ufvs[r.getUserIndex()] * ifvs[r.getItemIndex()];
            estimates[r.getIndex()] = clamp.apply(r.getUserId(), r.getItemId(), est + offset);
        }
    }
}
