/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import org.lenskit.api.ItemBasedItemRecommender;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Base class to make it easier to implement {@link ItemBasedItemRecommender}.  All methods delegate to
 * {@link #recommendRelatedItemsWithDetails(Set, int, Set, Set)}.
 */
public abstract class AbstractItemBasedItemRecommender implements ItemBasedItemRecommender {
    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #recommendRelatedItems(Set, int)}.
     */
    @Override
    public List<Long> recommendRelatedItems(long reference, int n) {
        return recommendRelatedItems(LongSets.singleton(reference), n);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #recommendRelatedItems(long, int)} with a size of -1.
     */
    @Override
    public List<Long> recommendRelatedItems(long reference) {
        return recommendRelatedItems(reference, -1);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #recommendRelatedItemsWithDetails(Set, int, Set, Set)}.
     */
    @Override
    public List<Long> recommendRelatedItems(Set<Long> basket, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        return recommendRelatedItemsWithDetails(basket, n, candidates, exclude).idList();
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #recommendRelatedItems(Set, int, Set, Set)} with default sets.
     */
    @Override
    public List<Long> recommendRelatedItems(Set<Long> basket, int n) {
        return recommendRelatedItems(basket, n, null, null);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #recommendRelatedItems(Set, int)} with a length of -1.
     */
    @Override
    public List<Long> recommendRelatedItems(Set<Long> basket) {
        return recommendRelatedItems(basket, -1);
    }
}
