/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
