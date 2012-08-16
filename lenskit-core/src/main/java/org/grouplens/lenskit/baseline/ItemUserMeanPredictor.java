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
package org.grouplens.lenskit.baseline;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.params.Damping;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.inject.Inject;
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
 * <p>It supports mean smoothing (see {@link Damping}).
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@DefaultProvider(ItemUserMeanPredictor.Provider.class)
@Shareable
public class ItemUserMeanPredictor extends ItemMeanPredictor {
    /**
     * A builder that creates ItemUserMeanPredictors.
     *
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     */
    public static class Provider implements javax.inject.Provider<ItemUserMeanPredictor> {
        private double damping = 0;
        private DataAccessObject dao;

        /**
         * Construct a new provider.
         * @param dao The DAO.
         * @param d The Bayesian mean damping term. A positive value biases means
         *          towards the global mean.
         */
        @Inject
        public Provider(@Transient DataAccessObject dao,
                        @Damping double d) {
            this.dao = dao;
            damping = d;
        }

        @Override
        public ItemUserMeanPredictor get() {
            Long2DoubleMap itemMeans = new Long2DoubleOpenHashMap();
            Cursor<Rating> ratings = dao.getEvents(Rating.class);
            double globalMean = computeItemAverages(ratings.fast().iterator(), damping, itemMeans);
            ratings.close();

            return new ItemUserMeanPredictor(itemMeans, globalMean, damping);
        }
    }

    private static final long serialVersionUID = 2L;

    /**
     * Create a new scorer, this assumes ownership of the given map.
     *
     * @param itemMeans The map of item means.
     * @param globalMean The global mean rating.
     * @param damping The damping term.
     */
    public ItemUserMeanPredictor(Long2DoubleMap itemMeans, double globalMean, double damping) {
        // FIXME Make this use a sparse vector
        super(itemMeans, globalMean, damping);
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
            total += r - getItemMean(iid);
        }
        return total / (values.size() + damping);
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
     */
    @Override
    public void predict(long user, SparseVector ratings,
                        MutableSparseVector scores, boolean predictSet) {
        double meanOffset = computeUserAverage(ratings);
        State state = predictSet ? State.EITHER : State.UNSET;
        for (VectorEntry e : scores.fast(state)) {
            scores.set(e, meanOffset + getItemMean(e.getKey()));
        }
    }
}
