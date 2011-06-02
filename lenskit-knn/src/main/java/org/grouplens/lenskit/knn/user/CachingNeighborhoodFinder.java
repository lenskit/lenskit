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
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.knn.params.UserSimilarity;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.grouplens.lenskit.params.PredictNormalizer;
import org.grouplens.lenskit.params.meta.Built;

/**
 * User-user CF implementation that caches user data for faster computation.
 *
 * <p>This implementation does nothing to update its caches, so it does not
 * update to reflect changes in ratings by users other than the current user.
 *
 * @todo Make it support updating its caches in response to changes in the data
 * source.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 *
 */
@Built
public class CachingNeighborhoodFinder implements NeighborhoodFinder {
    /**
     * Builder for creating CachingNeighborhoodFinders.
     * 
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     */
    public static class Builder extends RecommenderComponentBuilder<CachingNeighborhoodFinder> {
        private final int neighborhoodSize;
        private final Similarity<? super SparseVector> similarity;
        private final UserRatingVectorNormalizer normalizer;
        private final RatingDataAccessObject dao;
        
        public Builder(RatingDataAccessObject dao,
                       @UserSimilarity Similarity<? super SparseVector> similarity,
                       @NeighborhoodSize int neighborhoodSize,
                       @PredictNormalizer UserRatingVectorNormalizer normalizer) {
            this.neighborhoodSize = neighborhoodSize;
            this.similarity = similarity;
            this.dao = dao;
            this.normalizer = normalizer;
        }
        
        @Override
        public CachingNeighborhoodFinder build() {
            int nusers = 0;
            Long2ObjectMap<Collection<UserRatingProfile>> cache = new Long2ObjectOpenHashMap<Collection<UserRatingProfile>>();
            Cursor<UserRatingProfile> users = dao.getUserRatingProfiles();
            try {
                LongSet visitedItems = new LongOpenHashSet();
                for(UserRatingProfile user: users) {
                    visitedItems.clear();
                    nusers++;
                    for (Rating r: user.getRatings()) {
                        long iid = r.getItemId();
                        if (!visitedItems.contains(iid)) {
                            Collection<UserRatingProfile> cxn = cache.get(iid);
                            if (cxn == null) {
                                cxn = new ArrayList<UserRatingProfile>(100);
                                cache.put(iid, cxn);
                            }
                            cxn.add(user);
                        }
                    }
                }
            } finally {
                users.close();
            }
            for (Collection<UserRatingProfile> c: cache.values()) {
                ((ArrayList<UserRatingProfile>) c).trimToSize();
            }
            
            return new CachingNeighborhoodFinder(similarity, neighborhoodSize,
                                                 nusers, cache, normalizer);
        }
    }
    
    private final Long2ObjectMap<Collection<UserRatingProfile>> cache;
    private final int userCount;
    private final Similarity<? super SparseVector> similarity;
    private final int neighborhoodSize;
    private final UserRatingVectorNormalizer normalizer;

    protected CachingNeighborhoodFinder(Similarity<? super SparseVector> sim, int nnbrs, int nusers,
                                        Long2ObjectMap<Collection<UserRatingProfile>> cache,
                                        UserRatingVectorNormalizer norm) {
        this.cache = cache;
        similarity = sim;
        neighborhoodSize = nnbrs;
        userCount = nusers;
        normalizer = norm;
        
    }

    @Override
    public Long2ObjectMap<? extends Collection<Neighbor>>
        findNeighbors(long uid, SparseVector vector, LongSet items) {

        if (items == null)
            items = cache.keySet();

        Long2ObjectMap<Collection<Neighbor>> neighborhoods =
            new Long2ObjectOpenHashMap<Collection<Neighbor>>(items.size());
        Long2ObjectMap<Neighbor> neighborCache = new Long2ObjectOpenHashMap<Neighbor>(userCount);
        
        MutableSparseVector nv = vector.mutableCopy();
        normalizer.normalize(uid, nv);

        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            final long item = iter.next();
            Collection<UserRatingProfile> users = cache.get(item);
            PriorityQueue<Neighbor> neighbors =
                new PriorityQueue<Neighbor>(neighborhoodSize + 1, Neighbor.SIMILARITY_COMPARATOR);
            neighborhoods.put(item, neighbors);
            if (users == null) continue;

            for (UserRatingProfile user: users) {
                final long id = user.getUser();
                Neighbor nbr = neighborCache.get(id);
                if (nbr == null) {
                    SparseVector v = user.getRatingVector();
                    MutableSparseVector urv = v.mutableCopy();
                    normalizer.normalize(uid, urv);
                    double sim = similarity.similarity(nv, urv);
                    nbr = new Neighbor(id, v.mutableCopy(), sim);
                    neighborCache.put(id, nbr);
                }
                neighbors.add(nbr);
                if (neighbors.size() > neighborhoodSize) {
                    assert neighbors.size() == neighborhoodSize + 1;
                    neighbors.remove();
                }
            }
        }

        return neighborhoods;
    }
}
