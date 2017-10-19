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

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ResultList;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Base class to ease implementation of item recommenders.
 */
public abstract class AbstractItemRecommender implements ItemRecommender {
    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #recommend(long, int)} with a length of -1.
     */
    @Override
    public List<Long> recommend(long user) {
        return recommend(user, -1);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #recommend(long, int, Set, Set)} with a length of -1 and null sets.
     */
    @Override
    public List<Long> recommend(long user, int n) {
        return recommend(user, n, null, null);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public List<Long> recommend(long user, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        return recommend(user, n, LongUtils.asLongSet(candidates), LongUtils.asLongSet(exclude));
    }

    /**
     * Primary method for implementing an item recommender without details.  The default implementation delegates
     * to {@link #recommendWithDetails(long, int, LongSet, LongSet)}.
     * @param user The user ID.
     * @param n The number of recommendations to produce, or a negative value to produce unlimited recommendations.
     * @param candidates The candidate items, or {@code null} for default.
     * @param exclude The exclude set, or {@code null} for default.
     * @return The result list.
     * @see #recommend(long, int, Set, Set)
     */
    protected List<Long> recommend(long user, int n, @Nullable LongSet candidates, @Nullable LongSet exclude) {
        return recommendWithDetails(user, n, candidates, exclude).idList();
    }

    @Override
    public ResultList recommendWithDetails(long user, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        return recommendWithDetails(user, n, LongUtils.asLongSet(candidates), LongUtils.asLongSet(exclude));
    }

    /**
     * Primary method for implementing an item recommender.
     * @param user The user ID.
     * @param n The number of recommendations to produce, or a negative value to produce unlimited recommendations.
     * @param candidates The candidate items, or {@code null} for default.
     * @param exclude The exclude set, or {@code null} for default.
     * @return The result list.
     * @see #recommendWithDetails(long, int, Set, Set)
     */
    protected abstract ResultList recommendWithDetails(long user, int n, @Nullable LongSet candidates, @Nullable LongSet exclude);
}
