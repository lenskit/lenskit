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
package org.lenskit.bias;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.inject.Inject;

/**
 * Bias model that provides global, user, and item biases.  The global and item biases are precomputed and are *not*
 * refreshed based on user data added since the model build, but the user bias (mean rating from the rating DAO) is
 * recomputed live based on a {@link RatingVectorPDAO}.
 *
 * **Note:** The {@link #getUserBiases()} method will always return an empty map.
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

    /**
     * Return an empty map.  **This may make this bias model unsuitable in some applications.**
     * @return An empty map.
     */
    @Override
    public Long2DoubleMap getUserBiases() {
        return Long2DoubleMaps.EMPTY_MAP;
    }

    @Override
    public Long2DoubleMap getItemBiases() {
        return delegate.getItemBiases();
    }
}
