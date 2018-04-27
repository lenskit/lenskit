/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.mf.funksvd;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.bias.BiasModel;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.data.ratings.RatingMatrixEntry;

import java.util.List;

/**
 * Rating estimates used while training the predictor.  An estimator can be constructed
 * using {@link FunkSVDUpdateRule#makeEstimator(RatingMatrix)}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public final class TrainingEstimator {
    private final List<RatingMatrixEntry> ratings;
    private final double[] estimates;
    private final PreferenceDomain domain;

    /**
     * Initialize the training estimator.
     *  @param snap     The getEntry snapshot.
     * @param baseline The baseline predictor.
     * @param dom      The getEntry domain (for clamping).
     */
    TrainingEstimator(RatingMatrix snap, BiasModel baseline, PreferenceDomain dom) {
        ratings = snap.getRatings();
        domain = dom;
        estimates = new double[ratings.size()];

        final LongCollection userIds = snap.getUserIds();
        LongIterator userIter = userIds.iterator();
        double global = baseline.getIntercept();

        for (RatingMatrixEntry r: snap.getRatings()) {
            double userBias = baseline.getUserBias(r.getUserId());
            double itemBias = baseline.getItemBias(r.getItemId());
            estimates[r.getIndex()] = global + userBias + itemBias;
        }
    }

    /**
     * Get the estimate for a getEntry.
     * @param pref The getEntry.
     * @return The estimate.
     */
    public double get(RatingMatrixEntry pref) {
        return estimates[pref.getIndex()];
    }

    /**
     * Update the current estimates with trained values for a new feature.
     * @param ufvs The user feature values.
     * @param ifvs The item feature values.
     */
    public void update(RealVector ufvs, RealVector ifvs) {
        for (RatingMatrixEntry r : ratings) {
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
