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
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import org.grouplens.lenskit.AbstractDynamicRatingPredictor;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.CollectionUtils;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserUserRatingPredictor extends AbstractDynamicRatingPredictor {
    protected final NeighborhoodFinder neighborhoodFinder;
    
    UserUserRatingPredictor(RatingDataAccessObject dao, NeighborhoodFinder nbrf) {
        super(dao);
        neighborhoodFinder = nbrf;
    }
    
    /**
     * Get predictions for a set of items.  Unlike the interface method, this
     * method can take a null <var>items</var> set, in which case it returns all
     * possible predictions.
     * @see RatingPredictor#predict(long, SparseVector, Collection)
     */
    @Override
    public SparseVector predict(long user, SparseVector ratings, @Nullable Collection<Long> items) {
        Long2ObjectMap<? extends Collection<Neighbor>> neighborhoods =
            neighborhoodFinder.findNeighbors(user, ratings, items != null ? new LongSortedArraySet(items) : null);
        long[] keys = CollectionUtils.fastCollection(items).toLongArray();
        if (!(items instanceof LongSortedSet))
            Arrays.sort(keys);
        double[] preds = new double[keys.length];
        for (int i = 0; i < keys.length; i++) {
            final long item = keys[i];
            double sum = 0;
            double weight = 0;
            Collection<Neighbor> nbrs = neighborhoods.get(item);
            if (nbrs != null) {
                for (final Neighbor n: neighborhoods.get(item)) {
                    weight += abs(n.similarity);
                    sum += n.similarity * n.ratings.get(item);
                }
                preds[i] = sum / weight;
            } else {
                preds[i] = Double.NaN;
            }
        }
        return SparseVector.wrap(keys, preds, true);
    }
}
