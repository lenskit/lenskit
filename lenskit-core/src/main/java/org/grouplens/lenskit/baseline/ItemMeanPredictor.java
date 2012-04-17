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
/**
 *
 */
package org.grouplens.lenskit.baseline;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.grapht.annotation.Transient;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.params.Damping;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rating scorer that returns the item's mean rating for all predictions.
 *
 * If the item has no ratings, the global mean rating is returned.
 *
 * This implements the baseline scorer <i>p<sub>u,i</sub> = µ + b<sub>i</sub></i>,
 * where <i>b<sub>i</sub></i> is the item's average rating (less the global
 * mean µ).
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 *
 */
@DefaultProvider(ItemMeanPredictor.Provider.class)
public class ItemMeanPredictor implements BaselinePredictor {
    /**
     * A builder to create ItemMeanPredictors.
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     *
     */
    public static class Provider implements javax.inject.Provider<ItemMeanPredictor> {
        private double damping = 0;
        private DataAccessObject dao;
        
        @Inject
        public Provider(@Transient DataAccessObject dao,
                        @Damping double damping) {
            this.dao = dao;
            this.damping = damping;
        }

        @Override
        public ItemMeanPredictor get() {
            Long2DoubleMap itemMeans = new Long2DoubleOpenHashMap();
            Cursor<Rating> ratings = dao.getEvents(Rating.class);
            double globalMean = computeItemAverages(
                ratings.fast().iterator(),
                damping, itemMeans);
            ratings.close();
            
            return new ItemMeanPredictor(itemMeans, globalMean, damping);
        }
    }

    private static final long serialVersionUID = 2L;
    private static final Logger logger = LoggerFactory.getLogger(ItemMeanPredictor.class);

    private final Long2DoubleMap itemMeans;
    protected final double globalMean;
    protected final double damping;

    /**
     * Construct a new scorer. This assumes ownership of the provided map.
     *
     * @param itemMeans A map of item IDs to their mean ratings.
     * @param globalMean The mean rating value for all items.
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
     * Compute item averages from a rating data source. Used to construct
     * predictors that need this data.
     *
     * <p>
     * This method's interface is a little weird, using an output parameter and
     * returning the global mean, so that we can compute the global mean and the
     * item means in a single pass through the data source.
     *
     * @param ratings The collection of preferences the averages are based on.
     * @param itemMeansResult A map in which the means should be stored.
     * @return The global mean rating. The item means are stored in
     *         <var>itemMeans</var>.
     */
    public static double computeItemAverages(Iterator<? extends Rating> ratings, double damping, Long2DoubleMap itemMeansResult) {
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

        while(ratings.hasNext()) {
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

        logger.debug("Computing item means, smoothing={}", damping);
        LongIterator items = itemCounts.keySet().iterator();
        while (items.hasNext()) {
            long iid = items.nextLong();
            double ct = itemCounts.get(iid) + damping;
            double t = itemMeansResult.get(iid) + damping * mean;
            double avg = 0.0;
            if (ct > 0) avg = t / ct - mean;
            itemMeansResult.put(iid, avg);
        }
        return mean;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
     */
    @Override
    public MutableSparseVector predict(UserVector ratings,
                                       Collection<Long> items) {
        long[] keys = CollectionUtils.fastCollection(items).toLongArray();
        if (!(items instanceof LongSortedSet))
            Arrays.sort(keys);
        double[] preds = new double[keys.length];
        for (int i = 0; i < keys.length; i++) {
            preds[i] = getItemMean(keys[i]);
        }
        return MutableSparseVector.wrap(keys, preds);
    }

    @Override
    public String toString() {
        String cls = getClass().getSimpleName();
        return String.format("%s(µ=%.3f, γ=%.2f)", cls, globalMean, damping);
    }
    
    protected double getItemMean(long id) {
        return globalMean + itemMeans.get(id);
    }
}
