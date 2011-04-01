package org.grouplens.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Interface for neighborhood-finding strategies. These strategies are used by
 * {@link UserUserRatingRecommender} to find neighbors for recommendation.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface NeighborhoodFinder {
    /**
     * Find neighboring users for particular items.
     * @param user The user ID.
     * @param ratings The user rating vector.
     * @param items The items we're trying to recommend, or <tt>null</tt> to get
     * get neighborhoods for all possible items.
     * @return A map from item IDs to user neighborhoods for all items for which
     * we can find neighboring users.
     */
    
    Long2ObjectMap<? extends Collection<Neighbor>> findNeighbors(long user,
            @Nonnull SparseVector ratings, @Nullable LongSet items);
}
