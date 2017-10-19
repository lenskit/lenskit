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
