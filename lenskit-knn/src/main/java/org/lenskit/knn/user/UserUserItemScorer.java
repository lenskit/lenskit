/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.knn.user;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.knn.NeighborhoodSize;
import org.lenskit.results.Results;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.collections.SortedListAccumulator;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
    private final UserNeighborhoodScorer neighborhoodScorer;
    private final int neighborhoodSize;

    @Inject
    public UserUserItemScorer(RatingVectorPDAO rvd, NeighborFinder nf,
                              UserVectorNormalizer norm,
                              UserNeighborhoodScorer scorer,
                              @NeighborhoodSize int nnbrs) {
        this.dao = rvd;
        neighborFinder = nf;
        normalizer = norm;
        neighborhoodScorer = scorer;
        neighborhoodSize = nnbrs;
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

        // Make the normalizing transform to reverse
        InvertibleFunction<Long2DoubleMap, Long2DoubleMap> xform = normalizer.makeTransformation(user, history);

        // And prepare results
        List<UserUserResult> rawResults = new ArrayList<>();
        LongIterator iter = itemSet.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            double sum = 0;
            double weight = 0;
            int count = 0;
            List<Neighbor> nbrs = neighborhoods.get(item);
            UserUserResult score = neighborhoodScorer.score(item, nbrs);

            if (score != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("result {}", score);
                }
                rawResults.add(score);
            }
        }

        // de-normalize the results
        Long2DoubleMap itemScores = new Long2DoubleOpenHashMap(rawResults.size());
        for (UserUserResult r: rawResults) {
            itemScores.put(r.getId(), r.getScore());
        }
        itemScores = xform.unapply(itemScores);

        // and finish up
        List<Result> results = new ArrayList<>(rawResults.size());
        for (UserUserResult r: rawResults) {
            results.add(r.copyBuilder()
                         .setScore(itemScores.get(r.getId()))
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

        Long2ObjectOpenHashMap<SortedListAccumulator<Neighbor>> heaps = new Long2ObjectOpenHashMap<>(items.size());
        for (LongIterator iter = items.iterator(); iter.hasNext();) {
            long item = iter.nextLong();
            heaps.put(item, SortedListAccumulator.decreasing(neighborhoodSize,
                                                             Neighbor.SIMILARITY_COMPARATOR));
        }

        for (Neighbor nbr: neighborFinder.getCandidateNeighbors(user, items)) {
            // TODO consider optimizing
            for (Long2DoubleMap.Entry e: Vectors.fastEntries(nbr.vector)) {
                final long item = e.getLongKey();
                SortedListAccumulator<Neighbor> heap = heaps.get(item);
                if (heap != null) {
                    heap.add(nbr);
                }
            }
        }
        Long2ObjectMap<List<Neighbor>> neighbors = new Long2ObjectOpenHashMap<>();
        Iterator<Long2ObjectMap.Entry<SortedListAccumulator<Neighbor>>> hiter =
                heaps.long2ObjectEntrySet().fastIterator();
        while (hiter.hasNext()) {
            Long2ObjectMap.Entry<SortedListAccumulator<Neighbor>> me = hiter.next();
            neighbors.put(me.getLongKey(), me.getValue().finish());
        }
        return neighbors;
    }
}
