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
package org.lenskit.api;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A map of results from a recommender operation.  This is returned from operations such as *predict* that provide
 * scores or values for a collection of items, but do not rank or find items.
 */
public interface ResultMap extends Map<Long,Result>, Iterable<Result> {
    /**
     * View this result set as a map from longs to doubles.
     * @return A map view of this result set.
     */
    Map<Long,Double> scoreMap();

    /**
     * Get a result without boxing the key.
     * @param key The item ID.
     * @return The result, or {@code null} if the key is not in the map.
     */
    @Nullable
    Result get(long key);

    /**
     * Get the score associated with an ID.
     * @param id The ID to query.
     * @return The score associated with `id`, or {@link Double#NaN} if there is no score.
     */
    double getScore(long id);
}
