/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.knn.MinNeighbors;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.VectorTransformation;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.PriorityQueue;

import static java.lang.Math.abs;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class UserUserItemScorer extends AbstractItemScorer {
    private static final Logger logger = LoggerFactory.getLogger(UserUserItemScorer.class);

    public static final Symbol NEIGHBORHOOD_SIZE_SYMBOL =
            Symbol.of("org.grouplens.lenskit.knn.user.NeighborhoodSize");
    public static final Symbol NEIGHBORHOOD_WEIGHT_SYMBOL =
            Symbol.of("org.grouplens.lenskit.knn.user.NeighborhoodWeight");

    private final UserEventDAO dao;
    protected final NeighborFinder neighborFinder;
    protected final UserVectorNormalizer normalizer;
    private final int neighborhoodSize;
    private final int minNeighborCount;
    private final Threshold userThreshold;

    @Inject
    public UserUserItemScorer(UserEventDAO dao, NeighborFinder nf,
                              UserVectorNormalizer norm,
                              @NeighborhoodSize int nnbrs,
                              @MinNeighbors int minNbrs,
                              @UserSimilarityThreshold Threshold thresh) {
        this.dao = dao;
        neighborFinder = nf;
        normalizer = norm;
        neighborhoodSize = nnbrs;
        minNeighborCount = minNbrs;
        userThreshold = thresh;
    }

    /**
     * Normalize all neighbor rating vectors, taking care to normalize each one
     * only once.
     *
     * FIXME: MDE does not like this method.
     *
     * @param neighborhoods
     */
    protected Long2ObjectMap<SparseVector> normalizeNeighborRatings(Collection<? extends Collection<Neighbor>> neighborhoods) {
        Long2ObjectMap<SparseVector> normedVectors =
                new Long2ObjectOpenHashMap<SparseVector>();
        for (Neighbor n : Iterables.concat(neighborhoods)) {
            if (!normedVectors.containsKey(n.user)) {
                normedVectors.put(n.user, normalizer.normalize(n.user, n.vector, null));
            }
        }
        return normedVectors;
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        UserHistory<Event> history = dao.getEventsForUser(user);
        if (history == null) {
            history = History.forUser(user);
        }
        logger.debug("Predicting for {} items for user {} with {} events",
                     scores.size(), user, history.size());

        Long2ObjectMap<? extends Collection<Neighbor>> neighborhoods =
                findNeighbors(history, scores.keyDomain());
        Long2ObjectMap<SparseVector> normedUsers =
                normalizeNeighborRatings(neighborhoods.values());

        MutableSparseVector sizeChan = scores.addChannelVector(NEIGHBORHOOD_SIZE_SYMBOL);
        MutableSparseVector weightChan = scores.addChannelVector(NEIGHBORHOOD_WEIGHT_SYMBOL);
        for (VectorEntry e : scores.fast(VectorEntry.State.EITHER)) {
            final long item = e.getKey();
            double sum = 0;
            double weight = 0;
            int count = 0;
            Collection<Neighbor> nbrs = neighborhoods.get(item);
            if (nbrs != null) {
                for (Neighbor n : nbrs) {
                    weight += abs(n.similarity);
                    sum += n.similarity * normedUsers.get(n.user).get(item);
                    count += 1;
                }
            }
            
            if (count >= minNeighborCount) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Total neighbor weight for item {} is {} from {} neighbors",
                                 item, weight, count);
                }
                scores.set(e, sum / weight);
            } else {
                scores.unset(e);
            }
            sizeChan.set(e, count);
            weightChan.set(e,weight);
        }

        // Denormalize and return the results
        SparseVector urv = RatingVectorUserHistorySummarizer.makeRatingVector(history);
        VectorTransformation vo = normalizer.makeTransformation(history.getUserId(), urv);
        vo.unapply(scores);
    }

    /**
     * Find the neighbors for a user with respect to a collection of items.
     * For each item, the {@var neighborhoodSize} users closest to the
     * provided user are returned.
     *
     * @param user  The user's rating vector.
     * @param items The items for which neighborhoods are requested.
     * @return A mapping of item IDs to neighborhoods.
     */
    protected Long2ObjectMap<? extends Collection<Neighbor>>
    findNeighbors(@Nonnull UserHistory<? extends Event> user, @Nonnull LongSet items) {
        Preconditions.checkNotNull(user, "user profile");
        Preconditions.checkNotNull(user, "item set");

        Long2ObjectMap<PriorityQueue<Neighbor>> heaps = new Long2ObjectOpenHashMap<PriorityQueue<Neighbor>>(items.size());
        for (LongIterator iter = items.iterator(); iter.hasNext();) {
            long item = iter.nextLong();
            heaps.put(item, new PriorityQueue<Neighbor>(neighborhoodSize + 1,
                                                        Neighbor.SIMILARITY_COMPARATOR));
        }

        int neighborsUsed = 0;
        for (Neighbor nbr: neighborFinder.getCandidateNeighbors(user, items)) {
            for (VectorEntry e: nbr.vector.fast()) {
                final long item = e.getKey();
                PriorityQueue<Neighbor> heap = heaps.get(item);
                if (heap != null) {
                    heap.add(nbr);
                    if (heap.size() > neighborhoodSize) {
                        assert heap.size() == neighborhoodSize + 1;
                        heap.remove();
                    } else {
                        neighborsUsed += 1;
                    }
                }
            }
        }
        logger.debug("using {} neighbors across {} items",
                     neighborsUsed, items.size());
        return heaps;
    }
}
