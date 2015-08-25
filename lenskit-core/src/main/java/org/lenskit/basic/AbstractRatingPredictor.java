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
package org.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongSets;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Base class to make item scorers easier to implement. Delegates all score methods to
 * {@link #predictWithDetails(long, Collection)}.
 *
 * @since 3.0
 */
public abstract class AbstractRatingPredictor implements RatingPredictor {
    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #predictWithDetails(long, Collection)}.
     */
    @Override
    public Result predict(long user, long item) {
        ResultMap results = predictWithDetails(user, LongSets.singleton(item));
        return results.get(item);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #predictWithDetails(long, Collection)}.
     */
    @Nonnull
    @Override
    public Map<Long, Double> predict(long user, @Nonnull Collection<Long> items) {
        ResultMap results = predictWithDetails(user, items);
        return results.scoreMap();
    }
}
