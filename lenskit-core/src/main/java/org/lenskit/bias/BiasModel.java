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

/**
 * Interface for bias models that can be based on the user, item, or both.
 */
public interface BiasModel {
    /**
     * Get the global bias (intercept).
     * @return The global bias.
     */
    double getIntercept();

    /**
     * Get the user bias.
     * @param user The user ID.
     * @return The bias for the specified user, or 0 if the user's bias is unknown.
     */
    double getUserBias(long user);

    /**
     * Get a set of user biases.
     * @param users The users whose biases are to be returned.
     * @return A mapping from user IDs to biases. It is not guaranteed to contain all user IDs in its key set, but its
     * {@link Long2DoubleMap#defaultReturnValue()} will be 0.
     */
    Long2DoubleMap getUserBiases(LongSet users);

    /**
     * Get the map of all known user biases.
     * @return The set of all user biases.
     */
    Long2DoubleMap getUserBiases();

    /**
     * Get the item bias.
     * @param item The item ID.
     * @return The bias for the specified ite, or 0 if the item's bias is unknown.
     */
    double getItemBias(long item);

    /**
     * Get a set of item biases.
     * @param items The items whose biases are to be returned.
     * @return A mapping from item IDs to biases. It is not guaranteed to contain all item IDs in its key set, but its
     * {@link Long2DoubleMap#defaultReturnValue()} will be 0.
     */
    Long2DoubleMap getItemBiases(LongSet items);

    /**
     * Get the map of all known item biases.
     * @return The set of all item biases.
     */
    Long2DoubleMap getItemBiases();
}
