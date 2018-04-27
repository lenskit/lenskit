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
