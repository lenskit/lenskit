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
/**
 *
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
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Iterator;

import static org.grouplens.lenskit.vectors.VectorEntry.State;

/**
 * Rating scorer that returns the item's mean rating for all predictions.
 *
 * If the item has no ratings, the global mean rating is returned.
 *
 * This implements the baseline scorer <i>p<sub>u,i</sub> = µ + b<sub>i</sub></i>,
 * where <i>b<sub>i</sub></i> is the item's average rating (less the global
 * mean µ).
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(ItemMeanPredictor.Builder.class)
@Shareable
public class ItemMeanPredictor extends AbstractBaselinePredictor {
    /**
     * A builder to create ItemMeanPredictors.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder implements Provider<ItemMeanPredictor> {
        private double damping = 0;
        private DataAccessObject dao;

        /**
         * Construct a new provider.
         *
         * @param dao     The DAO.
         * @param damping The Bayesian mean damping term. It biases means toward the
         *                global mean.
         */
        @Inject
        public Builder(@Transient DataAccessObject dao,
                       @MeanDamping double damping) {
            this.dao = dao;
            this.damping = damping;
        }

        @Override
        public ItemMeanPredictor get() {
            Long2DoubleMap itemMeans = new Long2DoubleOpenHashMap();
            Cursor<Rating> ratings = dao.getEvents(Rating.class);
            double globalMean;
            try {
                globalMean = computeItemAverages(ratings.fast().iterator(),
                                                 damping, itemMeans);
            } finally {
                ratings.close();
            }

            return new ItemMeanPredictor(itemMeans, globalMean, damping);
        }
    }

    private static final long serialVersionUID = 2L;
    private static final Logger logger = LoggerFactory.getLogger(ItemMeanPredictor.class);

    private final Long2DoubleMap itemMeans;  // offsets from the global mean
    protected final double globalMean;
    protected final double damping;

    /**
     * Construct a new scorer. This assumes ownership of the provided map.
     *
     * @param itemMeans  A map of item IDs to their mean ratings.
     * @param globalMean The mean rating value for all items.
     * @param damping    The damping factor.
     */
    public ItemMeanPredictor(Long2DoubleMap itemMeans, double globalMean, double damping) {
        if (itemMeans instanceof Serializable) {
            this.itemMeans = itemMeans;
        } else {
            this.itemMeans = new Long2DoubleOpenHashMap(itemMeans);
        }
        this.globalMean = globalMean;
        this.damping = damping;
    }

    /**
     * Compute item averages from a rating data source. Used in
     * predictors that need this data.  Note that item averages 
     * are actually offsets from the global mean.
     *
     * <p>
     * This method's interface is a little weird, using an output parameter and
     * returning the global mean, so that we can compute the global mean and the
     * item means in a single pass through the data source.
     *
     * @param ratings         The collection of preferences the averages are based on.
     *                        This can be a fast iterator.
     * @param itemMeansResult A map in which the means should be stored.
     * @param damping         The damping term.
     * @return The global mean rating. The item means are stored in
     *         {@var itemMeans}.
     */
    public static double computeItemAverages(Iterator<? extends Rating> ratings, double damping,
                                             Long2DoubleMap itemMeansResult) {
        // We iterate the loop to compute the global and per-item mean
        // ratings.  Subtracting the global mean from each per-item mean
        // is equivalent to averaging the offsets from the global mean, so
        // we can compute the means in parallel and subtract after a single
        // pass through the data.
        double total = 0.0;
        int count = 0;
        itemMeansResult.defaultReturnValue(0.0);
        Long2IntMap itemCounts = new Long2IntOpenHashMap();
        itemCounts.defaultReturnValue(0);

        while (ratings.hasNext()) {
            Preference r = ratings.next().getPreference();
            if (r == null) {
                continue; // skip unrates
            }

            long i = r.getItemId();
            double v = r.getValue();
            total += v;
            count++;
            itemMeansResult.put(i, v + itemMeansResult.get(i));
            itemCounts.put(i, 1 + itemCounts.get(i));
        }

        final double mean = count > 0 ? total / count : 0;
        logger.debug("Computed global mean {} for {} items",
                     mean, itemMeansResult.size());

        logger.debug("Computing item means, damping={}", damping);
        LongIterator items = itemCounts.keySet().iterator();
        while (items.hasNext()) {
            long iid = items.nextLong();
            // the number of ratings for this item
            final int n = itemCounts.get(iid);
            // compute the total offset - subtract n means from total
            final double t = itemMeansResult.get(iid) - n * mean;
            // we pretend there are damping additional ratings with no offset
            final double ct = n + damping;
            // average goes to 0 if there are no ratings (shouldn't happen, b/c how did we get the item?)
            double avg = 0.0;
            if (ct > 0) {
                avg = t / ct;
            }
            itemMeansResult.put(iid, avg);
        }
        return mean;
    }

    @Override
    public void predict(long user, MutableSparseVector items, boolean predictSet) {
        State state = predictSet ? State.EITHER : State.UNSET;
        for (VectorEntry e : items.fast(state)) {
            items.set(e, getItemMean(e.getKey()));
        }
    }

    @Override
    public String toString() {
        String cls = getClass().getSimpleName();
        return String.format("%s(µ=%.3f, γ=%.2f)", cls, globalMean, damping);
    }

    /**
     * Get the mean for a particular item.
     *
     * @param id The item ID.
     * @return The item's mean rating.
     */
    protected double getItemMean(long id) {
        return globalMean + itemMeans.get(id);
    }
}
