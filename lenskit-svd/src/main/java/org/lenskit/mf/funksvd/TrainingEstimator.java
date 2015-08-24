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
package org.lenskit.mf.funksvd;

import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.api.ItemScorer;
import org.lenskit.util.collections.LongUtils;

import java.util.Collection;

/**
 * Rating estimates used while training the predictor.  An estimator can be constructed
 * using {@link FunkSVDUpdateRule#makeEstimator(PreferenceSnapshot)}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public final class TrainingEstimator {
    private final Collection<IndexedPreference> ratings;
    private final double[] estimates;
    private final PreferenceDomain domain;

    /**
     * Initialize the training estimator.
     *
     * @param snap     The preference snapshot.
     * @param baseline The baseline predictor.
     * @param dom      The preference domain (for clamping).
     */
    TrainingEstimator(PreferenceSnapshot snap, ItemScorer baseline, PreferenceDomain dom) {
        ratings = snap.getRatings();
        domain = dom;
        estimates = new double[ratings.size()];

        final LongCollection userIds = snap.getUserIds();
        LongIterator userIter = userIds.iterator();
        while (userIter.hasNext()) {
            long uid = userIter.nextLong();
            SparseVector rvector = snap.userRatingVector(uid);
            Long2DoubleFunction blpreds = LongUtils.asLong2DoubleFunction(baseline.score(uid, rvector.keySet()));

            for (IndexedPreference r : snap.getUserRatings(uid)) {
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
    public void update(RealVector ufvs, RealVector ifvs) {
        for (IndexedPreference r : ratings) {
            int idx = r.getIndex();
            double est = estimates[idx];
            est += ufvs.getEntry(r.getUserIndex()) * ifvs.getEntry(r.getItemIndex());
            if (domain != null) {
                est = domain.clampValue(est);
            }
            estimates[idx] = est;
        }
    }
}
