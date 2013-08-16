/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.GlobalItemRecommender;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.scored.ScoredId;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;


/**
 * Base class for item recommenders. It implements all methods required by
 * {@link GlobalItemRecommender} by delegating them to general methods with
 * fastutil-based interfaces.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractGlobalItemRecommender implements GlobalItemRecommender {
    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override
    public List<ScoredId> globalRecommend(Set<Long> items) {
        return globalRecommend(items, -1, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override
    public List<ScoredId> globalRecommend(Set<Long> items, int n) {
        return globalRecommend(items, n, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override
    public List<ScoredId> globalRecommend(Set<Long> items, @Nullable Set<Long> candidates) {
        return globalRecommend(items, -1, candidates, null);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #globalRecommend(LongSet, int, LongSet, LongSet)}.
     */
    @Override
    public List<ScoredId> globalRecommend(Set<Long> items, int n, @Nullable Set<Long> candidates,
                                          @Nullable Set<Long> exclude) {
        LongSet it = LongUtils.fastSet(items);
        LongSet cs = LongUtils.fastSet(candidates);
        LongSet es = LongUtils.fastSet(exclude);
        return globalRecommend(it, n, cs, es);
    }


    /**
     * Implementation method for global item recommendation.
     *
     * @param items      The items ID.
     * @param n          The number of items to return, or negative to return all
     *                   possible items.
     * @param candidates The candidate set.
     * @param exclude    The set of excluded items, or {@code null} to use the
     *                   default exclude set.
     * @return A scored list of item IDs.
     * @see GlobalItemRecommender#globalRecommend(Set, int, Set, Set)
     */
    protected abstract List<ScoredId> globalRecommend(LongSet items, int n,
                                                      @Nullable LongSet candidates,
                                                      @Nullable LongSet exclude);

}
