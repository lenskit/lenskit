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
import org.lenskit.api.ItemBasedItemScorer;
import org.lenskit.api.Result;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Base class to make it easier to implement {@link ItemBasedItemScorer}.  All methods delegate to
 * {@link #scoreRelatedItemsWithDetails(Collection, Collection)}.
 */
public abstract class AbstractItemBasedItemScorer implements ItemBasedItemScorer {
    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #scoreRelatedItemsWithDetails(Collection, Collection)}.
     */
    @Nonnull
    @Override
    public Map<Long, Double> scoreRelatedItems(@Nonnull Collection<Long> basket, @Nonnull Collection<Long> items) {
        return scoreRelatedItemsWithDetails(basket, items).scoreMap();
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #scoreRelatedItemsWithDetails(Collection, Collection)}.
     */
    @Override
    public Result scoreRelatedItem(@Nonnull Collection<Long> basket, long item) {
        return scoreRelatedItemsWithDetails(basket, LongSets.singleton(item)).get(item);
    }
}
