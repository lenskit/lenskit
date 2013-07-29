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
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.scored.ScoredId;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;


/**
 * Base class for item recommenders. It implements all methods required by
 * {@link ItemRecommender} by delegating them to general methods with
 * fastutil-based interfaces.
 */
public abstract class AbstractItemRecommender implements ItemRecommender {
    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public List<ScoredId> recommend(long user) {
        return recommend(user, -1, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public List<ScoredId> recommend(long user, int n) {
        return recommend(user, n, null, null);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public List<ScoredId> recommend(long user, Set<Long> candidates) {
        return recommend(user, -1, candidates, null);
    }

    /**
     * {@inheritDoc}
     * <p>Delegates to {@link #recommend(long, int, LongSet, LongSet)}.
     */
    @Override
    public List<ScoredId> recommend(long user, int n,
                                    @Nullable Set<Long> candidates,
                                    @Nullable Set<Long> exclude) {
        LongSet cs = CollectionUtils.fastSet(candidates);
        LongSet es = CollectionUtils.fastSet(exclude);
        return recommend(user, n, cs, es);
    }

    /**
     * Implementation method for ID-based recommendation.  All other ID-based methods are
     * implemented in terms of this one.
     *
     * @param user       The user ID.
     * @param n          The number of items to return, or negative to return all possible items.
     * @param candidates The candidate set, or {@code null} to use a default set of candidates.
     * @param exclude    The set of excluded items, or {@code null} to use the default exclude set.
     * @return A scored list of item IDs.
     * @see ItemRecommender#recommend(long, int, Set, Set)
     */
    protected abstract List<ScoredId> recommend(long user, int n,
                                                @Nullable LongSet candidates,
                                                @Nullable LongSet exclude);
}
