/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.knn.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.knn.MinNeighbors;
import org.lenskit.knn.NeighborhoodSize;
import org.lenskit.results.Results;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.collections.SortedListAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;

/**
 * Score items with user-user collaborative filtering.
 *
 * The detailed results returned by this scorer are of type {@link UserUserResult}.
 */
public class UserUserItemScorer extends AbstractItemScorer {
    private static final Logger logger = LoggerFactory.getLogger(UserUserItemScorer.class);

    private final RatingVectorPDAO dao;
    protected final NeighborFinder neighborFinder;
    protected final UserVectorNormalizer normalizer;
    private final int neighborhoodSize;
    private final int minNeighborCount;
    private final Threshold userThreshold;

    @Inject
    public UserUserItemScorer(RatingVectorPDAO rvd, NeighborFinder nf,
                              UserVectorNormalizer norm,
                              @NeighborhoodSize int nnbrs,
                              @MinNeighbors int minNbrs,
                              @UserSimilarityThreshold Threshold thresh) {
        this.dao = rvd;
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
     * @param neighborhoods The neighborhoods to search.
     */
    protected Long2ObjectMap<Long2DoubleMap> normalizeNeighborRatings(Collection<List<Neighbor>> neighborhoods) {
        Long2ObjectMap<Long2DoubleMap> normedVectors =
                new Long2ObjectOpenHashMap<>();
        for (Neighbor n : Iterables.<Neighbor>concat(neighborhoods)) {
            if (!normedVectors.containsKey(n.user)) {
                normedVectors.put(n.user, normalizer.makeTransformation(n.user, n.vector).apply(n.vector));
            }
        }
        return normedVectors;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        Long2DoubleMap history = dao.userRatingVector(user);

        logger.debug("Predicting for {} items for user {} with {} events",
                     items.size(), user, history.size());

        LongSortedSet itemSet = LongUtils.packedSet(items);
        Long2ObjectMap<List<Neighbor>> neighborhoods =
                findNeighbors(user, itemSet);
        Long2ObjectMap<Long2DoubleMap> normedUsers =
                normalizeNeighborRatings(neighborhoods.values());

        // Make the normalizing transform to reverse
        InvertibleFunction<Long2DoubleMap, Long2DoubleMap> xform = normalizer.makeTransformation(user, history);

        // And prepare results
        List<ResultBuilder> resultBuilders = new ArrayList<>();
        LongIterator iter = itemSet.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
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

            if (count >= minNeighborCount && weight > 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Total neighbor weight for item {} is {} from {} neighbors",
                                 item, weight, count);
                }
                resultBuilders.add(UserUserResult.newBuilder()
                                                 .setItemId(item)
                                                 .setRawScore(sum / weight)
                                                 .setNeighborhoodSize(count)
                                                 .setTotalWeight(weight));
            }
        }

        // de-normalize the results
        Long2DoubleMap itemScores = new Long2DoubleOpenHashMap(resultBuilders.size());
        for (ResultBuilder rb: resultBuilders) {
            itemScores.put(rb.getItemId(), rb.getRawScore());
        }
        itemScores = xform.unapply(itemScores);

        // and finish up
        List<Result> results = new ArrayList<>(resultBuilders.size());
        for (ResultBuilder rb: resultBuilders) {
            results.add(rb.setScore(itemScores.get(rb.getItemId()))
                          .build());
        }

        return Results.newResultMap(results);
    }

    /**
     * Find the neighbors for a user with respect to a collection of items.
     * For each item, the <var>neighborhoodSize</var> users closest to the
     * provided user are returned.
     *
     * @param user  The user's rating vector.
     * @param items The items for which neighborhoods are requested.
     * @return A mapping of item IDs to neighborhoods.
     */
    protected Long2ObjectMap<List<Neighbor>>
    findNeighbors(long user, @Nonnull LongSet items) {
        Preconditions.checkNotNull(user, "user profile");
        Preconditions.checkNotNull(user, "item set");

        Long2ObjectMap<SortedListAccumulator<Neighbor>> heaps = new Long2ObjectOpenHashMap<>(items.size());
        for (LongIterator iter = items.iterator(); iter.hasNext();) {
            long item = iter.nextLong();
            heaps.put(item, SortedListAccumulator.decreasing(neighborhoodSize,
                                                             Neighbor.SIMILARITY_COMPARATOR));
        }

        for (Neighbor nbr: neighborFinder.getCandidateNeighbors(user, items)) {
            // TODO consider optimizing
            for (Long2DoubleMap.Entry e: nbr.vector.long2DoubleEntrySet()) {
                final long item = e.getLongKey();
                SortedListAccumulator<Neighbor> heap = heaps.get(item);
                if (heap != null) {
                    heap.add(nbr);
                }
            }
        }
        Long2ObjectMap<List<Neighbor>> neighbors = new Long2ObjectOpenHashMap<>();
        for (Map.Entry<Long,SortedListAccumulator<Neighbor>> me: heaps.entrySet()) {
            neighbors.put(me.getKey(), me.getValue().finish());
        }
        return neighbors;
    }
}
