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
