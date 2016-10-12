/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.bias;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.inject.Inject;

/**
 * Bias model that provides global, user, and item biases.  The global and item biases are precomputed and are *not*
 * refreshed based on user data added since the model build, but the user bias (mean rating from the rating DAO) is
 * recomputed live based on a {@link RatingVectorPDAO}.
 */
public final class LiveUserItemBiasModel implements BiasModel{
    private final ItemBiasModel delegate;
    private final RatingVectorPDAO dao;

    /**
     * Construct a new bias model.
     * @param base An item bias model to use as the base model.
     * @param dao The rating vector DAO to fetch user data.
     */
    @Inject
    public LiveUserItemBiasModel(ItemBiasModel base, RatingVectorPDAO dao) {
        delegate = base;
        this.dao = dao;
    }

    @Override
    public double getIntercept() {
        return delegate.getIntercept();
    }

    @Override
    public double getUserBias(long user) {
        Long2DoubleMap vec = dao.userRatingVector(user);
        if (vec.isEmpty()) {
            return 0;
        } else {
            double sum = 0;
            double mean = getIntercept();
            for (Long2DoubleMap.Entry e: vec.long2DoubleEntrySet()) {
                sum += e.getDoubleValue() - mean - getItemBias(e.getLongKey());
            }
            return sum / vec.size();
        }
    }

    @Override
    public double getItemBias(long item) {
        return delegate.getItemBias(item);
    }

    @Override
    public Long2DoubleMap getUserBiases(LongSet users) {
        SortedKeyIndex index = SortedKeyIndex.fromCollection(users);
        final int n = index.size();
        double[] values = new double[n];

        for (int i = 0; i < n; i++) {
            values[i] = getUserBias(index.getKey(i));
        }

        return Long2DoubleSortedArrayMap.wrap(index, values);
    }

    @Override
    public Long2DoubleMap getItemBiases(LongSet items) {
        return delegate.getItemBiases(items);
    }
}
