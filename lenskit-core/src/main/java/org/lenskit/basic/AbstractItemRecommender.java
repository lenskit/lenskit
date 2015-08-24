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
