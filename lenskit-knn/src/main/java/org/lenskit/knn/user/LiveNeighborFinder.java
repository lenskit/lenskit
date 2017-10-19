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
import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.knn.ScoreNormalizer;
import org.lenskit.knn.SimilarityNormalizer;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Iterator;

/**
 * Neighborhood finder that does a fresh search over the data source ever time.
 */
public class LiveNeighborFinder implements NeighborFinder {
    private static final Logger logger = LoggerFactory.getLogger(LiveNeighborFinder.class);

    private final UserSimilarity similarity;
    private final RatingVectorPDAO rvDAO;
    private final DataAccessObject dao;
    private final UserVectorNormalizer scoreNormalizer;
    private final UserVectorNormalizer similarityNormalizer;
    private final Threshold threshold;

    /**
     * Construct a new user neighborhood finder.
     *
     * @param rvd    The user rating vector dAO.
     * @param dao    The data access object.
     * @param sim    The similarity function to use.
     * @param scoreNorm The normalizer for normalizing user rating vectors.
     * @param simNorm   The normalizer for computing similarity between user rating/preference vectors.
     * @param thresh The threshold for user similarities.
     */
    @Inject
    public LiveNeighborFinder(RatingVectorPDAO rvd, DataAccessObject dao,
                              UserSimilarity sim,
                              @ScoreNormalizer UserVectorNormalizer scoreNorm,
                              @SimilarityNormalizer UserVectorNormalizer simNorm,
                              @UserSimilarityThreshold Threshold thresh) {
        similarity = sim;
        scoreNormalizer = scoreNorm;
        similarityNormalizer = simNorm;
        rvDAO = rvd;
        this.dao = dao;
        threshold = thresh;

        Preconditions.checkArgument(sim.isSparse(), "user similarity function is not sparse");
    }

    @Override
    public Iterable<Neighbor> getCandidateNeighbors(final long user, LongSet items) {
        Long2DoubleMap ratings = rvDAO.userRatingVector(user);
        if (ratings.isEmpty()) {
            return Collections.emptyList();
        }

        final Long2DoubleMap nratings = similarityNormalizer.makeTransformation(user, ratings)
                                                            .apply(ratings);
        final LongSet candidates = findCandidateNeighbors(user, nratings.keySet(), items);
        logger.debug("found {} candidate neighbors for {}", candidates.size(), user);
        return new Iterable<Neighbor>() {
            @Override
            public Iterator<Neighbor> iterator() {
                return new NeighborIterator(user, nratings, candidates);
            }
        };
    }

    /**
     * Get the IDs of the candidate neighbors for a user.
     * @param user The user.
     * @param userItems The user's rated items.
     * @param targetItems The set of target items.
     * @return The set of IDs of candidate neighbors.
     */
    private LongSet findCandidateNeighbors(long user, LongSet userItems, LongCollection targetItems) {
        LongSet users = new LongOpenHashSet(100);

        LongIterator items;
        if (userItems.size() < targetItems.size()) {
            items = userItems.iterator();
        } else {
            items = targetItems.iterator();
        }
        while (items.hasNext()) {
            LongSet iusers = dao.query(CommonTypes.RATING)
                    .withAttribute(CommonAttributes.ITEM_ID, items.nextLong())
                    .valueSet(CommonAttributes.USER_ID);
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

    @Nullable
    private Long2DoubleMap getUserRatingVector(long user) {
        Long2DoubleMap ratings = rvDAO.userRatingVector(user);
        return ratings.isEmpty() ? null : ratings;
    }

    private class NeighborIterator extends AbstractIterator<Neighbor> {
        private final long user;
        private final Long2DoubleMap userVector;
        private final LongIterator neighborIter;

        public NeighborIterator(long uid, Long2DoubleMap uvec, LongSet nbrs) {
            user = uid;
            userVector = uvec;
            neighborIter = nbrs.iterator();
        }
        @Override
        protected Neighbor computeNext() {
            while (neighborIter.hasNext()) {
                final long neighbor = neighborIter.nextLong();
                Long2DoubleMap rawRatings = getUserRatingVector(neighbor);
                if (rawRatings != null) {
                    rawRatings = LongUtils.frozenMap(rawRatings);
                    InvertibleFunction<Long2DoubleMap, Long2DoubleMap> xform = similarityNormalizer.makeTransformation(neighbor, rawRatings);
                    Long2DoubleMap nbrRatings = xform.apply(rawRatings);
                    final double sim = similarity.similarity(user, userVector, neighbor, nbrRatings);
                    if (acceptSimilarity(sim)) {
                        // we have found a neighbor
                        Long2DoubleMap ratings;
                        if (scoreNormalizer.equals(similarityNormalizer)) {
                            ratings = nbrRatings;
                        } else {
                            ratings = scoreNormalizer.makeTransformation(neighbor, rawRatings).apply(rawRatings);
                        }
                        return new Neighbor(neighbor, ratings, sim);
                    }
                }
            }
            // no neighbor found, done
            return endOfData();
        }
    }
}
