/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

/**
 * Neighborhood finder that does a fresh search over the data source ever time.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LiveNeighborFinder implements NeighborFinder {
    private static final Logger logger = LoggerFactory.getLogger(LiveNeighborFinder.class);

    private final UserSimilarity similarity;
    private final UserEventDAO userDAO;
    private final ItemEventDAO itemDAO;
    private final UserVectorNormalizer normalizer;
    private final Threshold threshold;

    /**
     * Construct a new user neighborhood finder.
     *
     * @param udao   The user-event DAO.
     * @param idao   The item-event DAO.
     * @param sim    The similarity function to use.
     * @param norm   The normalizer for user rating/preference vectors.
     * @param thresh The threshold for user similarities.
     */
    @Inject
    public LiveNeighborFinder(UserEventDAO udao, ItemEventDAO idao,
                              UserSimilarity sim,
                              UserVectorNormalizer norm,
                              @UserSimilarityThreshold Threshold thresh) {
        similarity = sim;
        normalizer = norm;
        userDAO = udao;
        itemDAO = idao;
        threshold = thresh;

        Preconditions.checkArgument(sim.isSparse(), "user similarity function is not sparse");
    }

    @Override
    public Iterable<Neighbor> getCandidateNeighbors(UserHistory<? extends Event> user, LongSet items) {
        final long uid = user.getUserId();
        SparseVector urs = RatingVectorUserHistorySummarizer.makeRatingVector(user);
        final ImmutableSparseVector nratings = normalizer.normalize(user.getUserId(), urs, null)
                                                   .freeze();
        final LongSet candidates = findCandidateNeighbors(uid, nratings, items);
        logger.debug("found {} candidate neighbors for {}", candidates.size(), uid);
        return new Iterable<Neighbor>() {
            @Override
            public Iterator<Neighbor> iterator() {
                return new NeighborIterator(uid, nratings, candidates);
            }
        };
    }

    /**
     * Get the IDs of the candidate neighbors for a user.
     * @param user The user.
     * @param uvec The user's normalized preference vector.
     * @param itemSet The set of target items.
     * @return The set of IDs of candidate neighbors.
     */
    private LongSet findCandidateNeighbors(long user, SparseVector uvec, LongCollection itemSet) {
        LongSet users = new LongOpenHashSet(100);
        LongSet userItems = uvec.keySet();

        LongIterator items;
        if (userItems.size() < itemSet.size()) {
            items = userItems.iterator();
        } else {
            items = itemSet.iterator();
        }
        while (items.hasNext()) {
            LongSet iusers = itemDAO.getUsersForItem(items.nextLong());
            if (iusers != null) {
                users.addAll(iusers);
            }
        }
        users.remove(user);

        return users;
    }

    /**
     * Check if a similarity is acceptable.
     *
     * @param sim The similarity to check.
     * @return {@code false} if the similarity is NaN, infinite, or rejected by the threshold;
     *         {@code true} otherwise.
     */
    private boolean acceptSimilarity(double sim) {
        return !Double.isNaN(sim) && !Double.isInfinite(sim) && threshold.retain(sim);
    }

    private MutableSparseVector getUserRatingVector(long user) {
        List<Rating> ratings = userDAO.getEventsForUser(user, Rating.class);
        if (ratings == null){
            return null;
        }
        return Ratings.userRatingVector(ratings);
    }

    private class NeighborIterator extends AbstractIterator<Neighbor> {
        private final long user;
        private final SparseVector userVector;
        private final LongIterator neighborIter;

        public NeighborIterator(long uid, SparseVector uvec, LongSet nbrs) {
            user = uid;
            userVector = uvec;
            neighborIter = nbrs.iterator();
        }
        @Override
        protected Neighbor computeNext() {
            while (neighborIter.hasNext()) {
                final long neighbor = neighborIter.nextLong();
                MutableSparseVector nbrRatings = getUserRatingVector(neighbor);
                if (nbrRatings != null) {
                    ImmutableSparseVector rawRatings = nbrRatings.immutable();
                    normalizer.normalize(neighbor, rawRatings, nbrRatings);
                    final double sim = similarity.similarity(user, userVector, neighbor, nbrRatings);
                    if (acceptSimilarity(sim)) {
                        // we have found a neighbor
                        return new Neighbor(neighbor, rawRatings, sim);
                    }
                }
            }
            // no neighbor found, done
            return endOfData();
        }
    }
}
