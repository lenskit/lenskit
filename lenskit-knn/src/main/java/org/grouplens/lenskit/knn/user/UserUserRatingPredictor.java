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
package org.grouplens.lenskit.knn.user;

import static java.lang.Math.abs;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Collection;

import javax.annotation.Nullable;

import org.grouplens.lenskit.AbstractItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.data.vector.UserRatingVector;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.norm.VectorTransformation;
import org.grouplens.lenskit.params.PredictNormalizer;
import org.grouplens.lenskit.util.LongSortedArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserUserRatingPredictor extends AbstractItemScorer {
    private static final Logger logger = LoggerFactory.getLogger(UserUserRatingPredictor.class);
    protected final NeighborhoodFinder neighborhoodFinder;
    protected final VectorNormalizer<? super UserRatingVector> normalizer;
    protected final BaselinePredictor baseline;
    
    public UserUserRatingPredictor(DataAccessObject dao, NeighborhoodFinder nbrf,
                                   @PredictNormalizer VectorNormalizer<? super UserRatingVector> norm, 
                                   @Nullable BaselinePredictor baseline) {
        super(dao);
        neighborhoodFinder = nbrf;
        normalizer = norm;
        this.baseline = baseline;
        logger.debug("Built recommender with baseline {}", baseline);
    }
    
    /**
     * Normalize all neighbor rating vectors, taking care to normalize each one
     * only once.
     * 
     * FIXME: MDE does not like this method.
     * 
     * @param neighborhoods
     * 
     */
    protected Reference2ObjectMap<UserRatingVector, SparseVector> normalizeNeighborRatings(Collection<? extends Collection<Neighbor>> neighborhoods) {
        Reference2ObjectMap<UserRatingVector, SparseVector> normedVectors =
            new Reference2ObjectOpenHashMap<UserRatingVector, SparseVector>();
        for (Neighbor n: Iterables.concat(neighborhoods)) {
            if (!normedVectors.containsKey(n.user)) {
                normedVectors.put(n.user, normalizer.normalize(n.user, null));
            }
        }
        return normedVectors;
    }
    
    /**
     * Get predictions for a set of items.  Unlike the interface method, this
     * method can take a null <var>items</var> set, in which case it returns all
     * possible predictions.
     * @see RatingPredictor#score(long, Collection)
     */
    @Override
    public SparseVector score(UserHistory<? extends Event> history,
                              @Nullable Collection<Long> items) {
        logger.trace("Predicting for user {} with {} events",
                     history.getUserId(), history.size());
        LongSortedSet iset;
        if (items == null)
            iset = null;
        else if (items instanceof LongSortedSet)
            iset = (LongSortedSet) items;
        else
            iset = new LongSortedArraySet(items);
        Long2ObjectMap<? extends Collection<Neighbor>> neighborhoods =
            neighborhoodFinder.findNeighbors(history, iset);
        Reference2ObjectMap<UserRatingVector, SparseVector> normedUsers =
            normalizeNeighborRatings(neighborhoods.values());
        long[] keys = iset.toLongArray();
        double[] preds = new double[keys.length];
        LongArrayList missing = new LongArrayList();
        for (int i = 0; i < keys.length; i++) {
            final long item = keys[i];
            double sum = 0;
            double weight = 0;
            Collection<Neighbor> nbrs = neighborhoods.get(item);
            if (nbrs != null && !nbrs.isEmpty()) {
                for (final Neighbor n: neighborhoods.get(item)) {
                    weight += abs(n.similarity);
                    sum += n.similarity * normedUsers.get(n.user).get(item);
                }
                logger.trace("Total neighbor weight for item {} is {}", item, weight);
                preds[i] = sum / weight;
            } else {
                preds[i] = Double.NaN;
                missing.add(item);
            }
        }
        
        // Use the baseline
        if (baseline != null && missing.size() > 0) {
            logger.trace("Filling in {} missing user with baseline", missing.size());
            LongSortedSet mset = LongSortedArraySet.ofList(missing);
            MutableSparseVector blpreds = baseline.predict(history.ratingVector(), mset);
            for (int i = 0; i < keys.length; i++) {
                // TODO make this a parallel walk to avoid excess log factor
                if (Double.isNaN(preds[i])) {
                    preds[i] = blpreds.get(keys[i]);
                }
            }
        }
        
        // Denormalize and return the results
        VectorTransformation vo = normalizer.makeTransformation(history.ratingVector());
        MutableSparseVector v = MutableSparseVector.wrap(keys, preds, true);
        logger.trace("Returning {} predictions (wanted {})", v.size(), items.size());
        vo.unapply(v);
        return v;
    }
}
