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
