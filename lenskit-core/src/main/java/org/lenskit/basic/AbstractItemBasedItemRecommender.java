package org.lenskit.basic;

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
