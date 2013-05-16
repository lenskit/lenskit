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
package org.grouplens.lenskit.baseline;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;

import static org.grouplens.lenskit.vectors.VectorEntry.State;

/**
 * Predictor that returns the user's mean offset from item mean rating for all
 * predictions.
 *
 * <p>This implements the baseline scorer <i>p<sub>u,i</sub> = µ + b<sub>i</sub> +
 * b<sub>u</sub></i>, where <i>b<sub>i</sub></i> is the item's average rating (less the global
 * mean <i>µ</i>), and <i>b<sub>u</sub></i> is the user's average offset (the average
 * difference between their ratings and the item-mean baseline).
 *
 * <p>It supports mean smoothing (see {@link MeanDamping}).
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(ItemUserMeanPredictor.Builder.class)
@Shareable
public class ItemUserMeanPredictor extends AbstractBaselinePredictor {
    private static final long serialVersionUID = 3L;
    private final ImmutableSparseVector itemMeans;
    private final ImmutableSparseVector userMeans;
    private final double globalMean;
    private final double damping;

    /**
     * A builder that creates ItemUserMeanPredictors.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder implements Provider<ItemUserMeanPredictor> {
        private double damping = 0;
        private DataAccessObject dao;

        /**
         * Construct a new provider.
         *
         * @param dao The DAO.
         * @param d   The Bayesian mean damping term. A positive value biases means
         *            towards the global mean.
         */
        @Inject
        public Builder(@Transient DataAccessObject dao,
                       @MeanDamping double d) {
            this.dao = dao;
            damping = d;
        }

        @Override
        public ItemUserMeanPredictor get() {
            final ImmutableSparseVector itemMeans;
            Cursor<Rating> ratings = dao.getEvents(Rating.class);
            double globalMean;
            try {
                Long2DoubleMap itemMeanAccum = new Long2DoubleOpenHashMap();
                globalMean = ItemMeanPredictor.computeItemAverages(ratings.fast().iterator(), damping,
                                                                   itemMeanAccum);
                itemMeans = new ImmutableSparseVector(itemMeanAccum);
            } finally {
                ratings.close();
            }

            // now compute the user offsets, must be separate pass :(
            MutableSparseVector userMeans;
            ratings = dao.getEvents(Rating.class);
            try {
                Long2DoubleMap uSums = new Long2DoubleOpenHashMap();
                Long2IntMap uCounts = new Long2IntOpenHashMap();
                for (Rating r: ratings.fast()) {
                    Preference p = r.getPreference();
                    if (p != null) {
                        long uid = p.getUserId();
                        double v = p.getValue() - itemMeans.get(p.getItemId()) - globalMean;
                        uSums.put(uid, uSums.get(uid) + v);
                        uCounts.put(uid, uCounts.get(uid) + 1);
                    }
                }
                userMeans = new MutableSparseVector(uSums);
                for (VectorEntry e: userMeans.fast()) {
                    userMeans.set(e, e.getValue() / (uCounts.get(e.getKey()) + damping));
                }
            } finally {
                ratings.close();
            }

            return new ItemUserMeanPredictor(itemMeans, userMeans.freeze(),
                                             globalMean, damping);
        }
    }

    /**
     * Create a new scorer, this assumes ownership of the given map.
     *
     * @param iMeans  The map of item means.
     * @param iMeans  The map of user means.
     * @param mean The global mean rating.
     * @param damp    The damping term.
     */
    public ItemUserMeanPredictor(ImmutableSparseVector iMeans,
                                 ImmutableSparseVector uMeans,
                                 double mean, double damp) {
        itemMeans = iMeans;
        userMeans = uMeans;
        globalMean = mean;
        damping = damp;
    }

    /**
     * Compute the mean offset in user rating from item mean rating.
     *
     * @param ratings the user's rating profile
     * @return the mean offset from item mean rating.
     */
    protected double computeUserAverage(SparseVector ratings) {
        if (ratings.isEmpty()) {
            return 0;
        }

        Collection<Double> values = ratings.values();
        double total = 0;

        for (VectorEntry rating : ratings.fast()) {
            double r = rating.getValue();
            long iid = rating.getKey();
            total += r - globalMean - itemMeans.get(iid, 0);
        }
        return total / (values.size() + damping);
    }

    @Override
    public void predict(long user, MutableSparseVector scores, boolean predictSet) {
        double meanOffset = userMeans.get(user, 0);
        State state = predictSet ? State.EITHER : State.UNSET;
        for (VectorEntry e : scores.fast(state)) {
            scores.set(e, meanOffset + globalMean + itemMeans.get(e.getKey(), 0));
        }
    }

    @Override
    public void predict(long user, SparseVector ratings,
                        MutableSparseVector scores, boolean predictSet) {
        double meanOffset = computeUserAverage(ratings);
        State state = predictSet ? State.EITHER : State.UNSET;
        for (VectorEntry e : scores.fast(state)) {
            scores.set(e, meanOffset + globalMean + itemMeans.get(e.getKey(), 0));
        }
    }
}
