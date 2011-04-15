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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.grouplens.lenskit.AbstractRecommenderComponentBuilder;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rating predictor that returns the item's mean rating for all predictions.
 *
 * If the item has no ratings, the global mean rating is returned.
 *
 * This implements the baseline predictor <i>p<sub>u,i</sub> = µ + b<sub>i</sub></i>,
 * where <i>b<sub>i</sub></i> is the item's average rating (less the global
 * mean µ).
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 *
 */
public class ItemMeanPredictor implements BaselinePredictor {
    /**
     * A builder to create ItemMeanPredictors.
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     *
     */
    public static class Builder extends AbstractRecommenderComponentBuilder<ItemMeanPredictor> {
        private double damping = 0;
        
        public void setDamping(double damping) {
            this.damping = damping;
        }
        
        @Override
        protected ItemMeanPredictor buildNew(RatingBuildContext context) {
            Long2DoubleMap itemMeans = new Long2DoubleOpenHashMap();
            double globalMean = computeItemAverages(context.getRatings().fastIterator(), damping, itemMeans);
            
            return new ItemMeanPredictor(itemMeans, globalMean);
        }
    }
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ItemMeanPredictor.class);
    
    private final Long2DoubleMap itemMeans;
    protected final double globalMean;

    /**
     * Construct a new predictor. This assumes ownership of the provided map.
     * @param ratings The rating data.
     * @param smoothing The smoothing factor (see
     * {@link #computeItemAverages(RatingDataSource, double, Long2DoubleMap)}).
     */
    protected ItemMeanPredictor(Long2DoubleMap itemMeans, double globalMean) {
        this.itemMeans = itemMeans;
        this.globalMean = globalMean;
    }

    /**
     * Compute item averages from a rating data source.  Used to construct
     * predictors that need this data.
     *
     * <p>This method's interface is a little weird, using an output parameter
     * and returning the global mean, so that we can compute the global mean
     * and the item means in a single pass through the data source.
     *
     * @param ratings The collection of ratings the averages are based on
     * @param smoothing The mean smoothing factor (see {@link MeanDamping} for how
     * this is used).
     * @param itemMeans A map in which the means should be stored.
     * @return The global mean rating.  The item means are stored in
     * <var>itemMeans</var>.
     */
    public static double computeItemAverages(Iterator<? extends Rating> ratings, double damping, Long2DoubleMap itemMeans) {
        // We iterate the loop to compute the global and per-item mean
        // ratings.  Subtracting the global mean from each per-item mean
        // is equivalent to averaging the offsets from the global mean, so
        // we can compute the means in parallel and subtract after a single
        // pass through the data.
        double total = 0.0;
        int count = 0;
        itemMeans.defaultReturnValue(0.0);
        Long2IntMap itemCounts = new Long2IntOpenHashMap();
        itemCounts.defaultReturnValue(0);

        while(ratings.hasNext()) {
            Rating r = ratings.next();
            long i = r.getItemId();
            double v = r.getRating();
            total += v;
            count++;
            itemMeans.put(i, v + itemMeans.get(i));
            itemCounts.put(i, 1 + itemCounts.get(i));
        }

        final double mean = count > 0 ? total / count : 0;
        logger.debug("Computed global mean {} for {} items",
                mean, itemMeans.size());

        logger.debug("Computing item means, smoothing={}", damping);

        LongIterator items = itemCounts.keySet().iterator();
        while (items.hasNext()) {
            long iid = items.nextLong();
            double ct = itemCounts.get(iid) + damping;
            double t = itemMeans.get(iid) + damping * mean;
            double avg = 0.0;
            if (ct > 0) avg = t / ct - mean;
            itemMeans.put(iid, avg);
        }
        return mean;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RatingPredictor#predict(long, java.util.Map, java.util.Collection)
     */
    @Override
    public MutableSparseVector predict(long user, SparseVector ratings,
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

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RatingPredictor#predict(long, java.util.Map, long)
     */
    @Override
    public ScoredId predict(long user, SparseVector ratings, long item) {
        return new ScoredId(item, getItemMean(item));
    }

    protected double getItemMean(long id) {
        return globalMean + itemMeans.get(id);
    }
}
