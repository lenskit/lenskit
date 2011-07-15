/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.user;

import static java.lang.Math.max;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.ItemEvent;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.data.vector.UserRatingVector;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.knn.params.UserSimilarity;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.params.UserRatingVectorNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Neighborhood finder that does a fresh search over the data source ever time.
 * 
 * <p>This rating vector has support for caching user rating vectors, where it
 * avoids rebuilding user rating vectors for users with no changed user. When
 * caching is enabled, it assumes that the underlying data is timestamped and
 * that the timestamps are well-behaved: if a rating has been added after the
 * currently cached rating vector was computed, then its timestamp is greater
 * than any timestamp seen while computing the cached vector.
 * 
 * <p>Currently, this cache is never cleared. This should probably be changed
 * sometime.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleNeighborhoodFinder implements NeighborhoodFinder, Serializable {
    private static final long serialVersionUID = -6324767320394518347L;
    private static final Logger logger = LoggerFactory.getLogger(SimpleNeighborhoodFinder.class);
    
    static class CacheEntry {
        final UserRatingVector user;
        final long lastRatingTimestamp;
        final int ratingCount;
        
        CacheEntry(UserRatingVector urv, long ts, int count) {
            user = urv;
            lastRatingTimestamp = ts;
            ratingCount = count;
        }
    }
    
    private final DataAccessObject dataSource;
    private final int neighborhoodSize;
    private final Similarity<? super SparseVector> similarity;
	private final VectorNormalizer<? super UserRatingVector> normalizer;
	private final Long2ObjectMap<CacheEntry> userVectorCache;

    /**
     * Construct a new user-user recommender.
     * @param data The data source to scan.
     * @param nnbrs The number of neighbors to consider for each item.
     * @param sim The similarity function to use.
     */
    public SimpleNeighborhoodFinder(DataAccessObject data,
                                    @NeighborhoodSize int nnbrs, 
                                    @UserSimilarity Similarity<? super SparseVector> sim,
                                    @UserRatingVectorNormalizer VectorNormalizer<? super UserRatingVector> norm) {
        dataSource = data;
        neighborhoodSize = nnbrs;
        similarity = sim;
        normalizer = norm;
        userVectorCache = new Long2ObjectOpenHashMap<CacheEntry>(500);
    }

    /**
     * Find the neighbors for a user with respect to a collection of items.
     * For each item, the <var>neighborhoodSize</var> users closest to the
     * provided user are returned.
     *
     * @param user The user's rating vector.
     * @param items The items for which neighborhoods are requested.
     * @return A mapping of item IDs to neighborhoods.
     */
    @Override
    public Long2ObjectMap<? extends Collection<Neighbor>> findNeighbors(UserRatingVector user, LongSet items) {
        Long2ObjectMap<PriorityQueue<Neighbor>> heaps =
            new Long2ObjectOpenHashMap<PriorityQueue<Neighbor>>(items != null ? items.size() : 100);
        
        MutableSparseVector nratings = normalizer.normalize(user, null);
        
        /* Find candidate neighbors. To reduce scanning, we limit users to those
         * rating target items. If the similarity is sparse and the user has
         * fewer items than target items, then we use the user's rated items to
         * attempt to minimize the number of users considered.
         */
        LongSet users = findRatingUsers(user.getUserId(), items);
        
        logger.trace("Found {} candidate neighbors", users.size());
        
        LongIterator uiter = users.iterator();
        while (uiter.hasNext()) {
            final long uid = uiter.nextLong();
            UserRatingVector urv = getUserRatingVector(uid);
            MutableSparseVector nurv = normalizer.normalize(urv, null);            
            
            final double sim = similarity.similarity(nratings, nurv);
            if (Double.isNaN(sim) || Double.isInfinite(sim))
                continue;
            final Neighbor n = new Neighbor(urv, sim);

            LongIterator iit = urv.keySet().iterator();
            ITEMS: while (iit.hasNext()) {
                final long item = iit.nextLong();
                if (items != null && !items.contains(item))
                    continue ITEMS;

                PriorityQueue<Neighbor> heap = heaps.get(item);
                if (heap == null) {
                    heap = new PriorityQueue<Neighbor>(neighborhoodSize + 1,
                            Neighbor.SIMILARITY_COMPARATOR);
                    heaps.put(item, heap);
                }
                heap.add(n);
                if (heap.size() > neighborhoodSize) {
                    assert heap.size() == neighborhoodSize + 1;
                    heap.remove();
                }
            }
        }
        return heaps;
    }

    /**
     * Find all users who have rated any of a set of items.
     * @param user The current user's ID (excluded from the returned set).
     * @param itemSet The set of items to look for.
     * @return The set of all users who have rated at least one item in <var>itemSet</var>.
     */
    private LongSet findRatingUsers(long user, LongCollection itemSet) {
        LongSet users = new LongOpenHashSet(100);
        
        LongIterator items = itemSet.iterator();
        while (items.hasNext()) {
            final long item = items.nextLong();
            Cursor<Rating> ratings = dataSource.getItemRatings(item);
            try {
                for (ItemEvent r: ratings) {
                    long uid = r.getUserId();
                    if (uid == user) continue;
                    users.add(uid);
                }
            } finally {
                ratings.close();
            }
        }
        
        return users;
    }
    
    /**
     * Look up the user's rating vector, using the cached version if possible.
     * @param user The user ID.
     * @return The user's rating vector.
     */
    private synchronized UserRatingVector getUserRatingVector(long user) {
        List<Rating> ratings = Cursors.makeList(dataSource.getUserRatings(user));
        CacheEntry e = userVectorCache.get(user);
        
        // check rating count
        if (e != null && e.ratingCount != ratings.size())
            e = null;
        
        // check max timestamp
        long ts = -1;
        if (e != null) {
            for (ItemEvent r: ratings) {
                ts = max(ts, r.getTimestamp());
            }
            if (ts != e.lastRatingTimestamp)
                e = null;
        }
        
        // create new cache entry
        if (e == null) {
            UserRatingVector v = UserRatingVector.fromRatings(user, ratings);
            e = new CacheEntry(v, ts, ratings.size());
            userVectorCache.put(user, e);
        }
        
        return e.user;
    }
}
