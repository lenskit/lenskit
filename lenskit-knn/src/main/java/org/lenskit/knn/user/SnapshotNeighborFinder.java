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

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.knn.SimilarityNormalizer;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.ThreadSafe;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Iterator;

/**
 * A neighborhood finder that has a snapshot of the rating data for efficiency.
 *
 * @since 2.1
 */
@ThreadSafe
public class SnapshotNeighborFinder implements NeighborFinder {
    private static final Logger logger = LoggerFactory.getLogger(SnapshotNeighborFinder.class);

    private final UserSnapshot snapshot;
    private final UserSimilarity similarity;
    private final RatingVectorPDAO rvDAO;
    private final UserVectorNormalizer similarityNormalizer;
    private final Threshold threshold;

    @Inject
    public SnapshotNeighborFinder(UserSnapshot snap,
                                  UserSimilarity sim,
                                  RatingVectorPDAO rvd,
                                  @SimilarityNormalizer UserVectorNormalizer simNorm,
                                  @UserSimilarityThreshold Threshold thresh) {
        snapshot = snap;
        similarity = sim;
        rvDAO = rvd;
        similarityNormalizer = simNorm;
        threshold = thresh;
    }

    @Override
    public Iterable<Neighbor> getCandidateNeighbors(final long user, LongSet items) {
        Long2DoubleMap urs = rvDAO.userRatingVector(user);
        if (urs.isEmpty()) {
            return Collections.emptyList();
        }

        final Long2DoubleMap normed = similarityNormalizer.makeTransformation(user, urs)
                                                          .apply(urs);
        assert normed != null;

        LongCollection qset = items;
        if (normed.size() < qset.size()) {
            qset = normed.keySet();
        }
        final LongSet candidates = new LongOpenHashSet();
        for (LongIterator iter = qset.iterator(); iter.hasNext();) {
            final long item = iter.nextLong();
            LongSet users = snapshot.getItemUsers(item);
            if (users != null) {
                candidates.addAll(users);
            }
        }
        candidates.remove(user);
        logger.debug("Found {} candidate neighbors for user {}", candidates.size(), user);
        return new Iterable<Neighbor>() {
            @Override
            public Iterator<Neighbor> iterator() {
                return new NeighborIterator(user, normed, candidates);
            }
        };
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

    private class NeighborIterator extends AbstractIterator<Neighbor> {
        private final long user;
        private final Long2DoubleMap userVector;
        private final LongIterator neighborIter;

        NeighborIterator(long uid, Long2DoubleMap uvec, LongSet nbrs) {
            user = uid;
            userVector = uvec;
            neighborIter = nbrs.iterator();
        }

        @Override
        protected Neighbor computeNext() {
            while (neighborIter.hasNext()) {
                final long neighbor = neighborIter.nextLong();
                Long2DoubleMap vector = snapshot.getNormalizedUserVector(neighbor);
                double sim = similarity.similarity(user, userVector, neighbor, vector);
                if (acceptSimilarity(sim)) {
                    return new Neighbor(neighbor, snapshot.getUserVector(neighbor), sim);
                }

            }
            // no neighbor found, done
            return endOfData();
        }
    }
}
