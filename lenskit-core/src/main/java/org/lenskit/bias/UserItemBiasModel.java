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
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import net.jcip.annotations.Immutable;
import java.io.Serializable;

/**
 * Bias model that provides global, user, and item biases.  The user and item biases are precomputed and are *not*
 * refreshed based on user data added since the model build.
 */
@Shareable
@Immutable
@DefaultProvider(UserItemAverageRatingBiasModelProvider.class)
public class UserItemBiasModel implements BiasModel, Serializable {
    private static final long serialVersionUID = 1L;

    private final double intercept;
    private final Long2DoubleSortedArrayMap userBiases;
    private final Long2DoubleSortedArrayMap itemBiases;

    /**
     * Construct a new user bias model.
     * @param global The global bias.
     * @param users The user biases.
     * @param items The item biases.
     */
    public UserItemBiasModel(double global, Long2DoubleMap users, Long2DoubleMap items) {
        intercept = global;
        userBiases = Long2DoubleSortedArrayMap.create(users);
        itemBiases = Long2DoubleSortedArrayMap.create(items);
    }

    @Override
    public double getIntercept() {
        return intercept;
    }

    @Override
    public double getUserBias(long user) {
        return userBiases.get(user);
    }

    @Override
    public Long2DoubleMap getUserBiases(LongSet users) {
        return userBiases.subMap(users);
    }

    @Override
    public Long2DoubleMap getUserBiases() {
        return userBiases;
    }

    @Override
    public double getItemBias(long item) {
        return itemBiases.get(item);
    }

    @Override
    public Long2DoubleMap getItemBiases(LongSet items) {
        return itemBiases.subMap(items);
    }

    @Override
    public Long2DoubleMap getItemBiases() {
        return itemBiases;
    }
}
