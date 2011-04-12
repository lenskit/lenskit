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

import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Arrays;
import java.util.Collection;

import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.CollectionUtils;

/**
 * Rating predictor that predicts a constant rating for all items.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ConstantPredictor implements BaselinePredictor {
    private static final long serialVersionUID = 1L;
    
    private final double value;

    /**
     * Construct a rating vector with the same rating for all items.
     * @param items The items to include in the vector.
     * @param value The rating/prediction to give.
     * @return A rating vector mapping all items in <var>items</var> to
     * <var>value</var>.
     */
    public static MutableSparseVector constantPredictions(Collection<Long> items, double value) {
        long[] keys = CollectionUtils.fastCollection(items).toLongArray();
        if (!(items instanceof LongSortedSet))
            Arrays.sort(keys);
        double[] preds = new double[keys.length];
        Arrays.fill(preds, value);
        return MutableSparseVector.wrap(keys, preds);
    }

    /**
     * Construct a new constant predictor.  This is exposed so other code
     * can use it as a fallback.
     * @param value
     */
    public ConstantPredictor(double value) {
        this.value = value;
    }

    @Override
    public MutableSparseVector predict(long user, SparseVector ratings, Collection<Long> items) {
        return constantPredictions(items, value);
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RatingPredictor#predict(java.lang.Object, java.util.Map, java.lang.Object)
     */
    @Override
    public ScoredId predict(long user, SparseVector profile, long item) {
        return new ScoredId(item, value);
    }
}
