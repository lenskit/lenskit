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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;
import java.util.PriorityQueue;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Neighborhood finder that does a fresh search over the data source ever time.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleNeighborhoodFinder implements NeighborhoodFinder {
    private static final Logger logger = LoggerFactory.getLogger(SimpleNeighborhoodFinder.class);
    
    /**
     * Builder for creating SimpleNeighborhoodFinders.
     * 
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     */
    public static class Builder extends AbstractNeighborhoodFinderBuilder<SimpleNeighborhoodFinder> {
        @Override
        protected SimpleNeighborhoodFinder buildNew(RatingBuildContext context) {
            return new SimpleNeighborhoodFinder(context.getDAO(),
                                                neighborhoodSize, similarity,
                                                normalizerBuilder.build(context));
        }
    }
    
    private final RatingDataAccessObject dataSource;
    private final int neighborhoodSize;
    private final Similarity<? super SparseVector> similarity;
	private final UserRatingVectorNormalizer normalizer;

    /**
     * Construct a new user-user recommender.
     * @param data The data source to scan.
     * @param nnbrs The number of neighbors to consider for each item.
     * @param sim The similarity function to use.
     */
    protected SimpleNeighborhoodFinder(RatingDataAccessObject data, int nnbrs, 
                                       Similarity<? super SparseVector> sim,
                                       UserRatingVectorNormalizer norm) {
        dataSource = data;
        neighborhoodSize = nnbrs;
        similarity = sim;
        normalizer = norm;
    }

    /**
     * Find the neighbors for a user with respect to a collection of items.
     * For each item, the <var>neighborhoodSize</var> users closest to the
     * provided user are returned.
     *
     * @param uid The user ID.
     * @param ratings The user's ratings vector.
     * @param items The items for which neighborhoods are requested.
     * @return A mapping of item IDs to neighborhoods.
     */
    @Override
    public Long2ObjectMap<? extends Collection<Neighbor>> findNeighbors(long uid, SparseVector ratings, LongSet items) {
        Long2ObjectMap<PriorityQueue<Neighbor>> heaps =
            new Long2ObjectOpenHashMap<PriorityQueue<Neighbor>>(items != null ? items.size() : 100);
        
        MutableSparseVector nratings = ratings.mutableCopy();
        normalizer.normalize(uid, nratings);
        
        LongSet users = findRatingUsers(dataSource, uid, ratings.keySet());
        LongIterator uiter = users.iterator();
        while (uiter.hasNext()) {
            final long user = uiter.nextLong();
            MutableSparseVector urv = Ratings.userRatingVector(dataSource.getUserRatings(user));
            MutableSparseVector nurv = urv.mutableCopy();
            normalizer.normalize(user, nurv);
            
            final double sim = similarity.similarity(nratings, nurv);
            final Neighbor n = new Neighbor(user, urv.mutableCopy(), sim);

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

    private LongSet findRatingUsers(RatingDataAccessObject dao,
                                    long user, LongCollection keySet) {
        LongSet users = new LongOpenHashSet(100);
        
        LongIterator items = keySet.iterator();
        while (items.hasNext()) {
            final long item = items.nextLong();
            Cursor<Rating> ratings = dao.getItemRatings(item);
            try {
                for (Rating r: ratings) {
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
}
