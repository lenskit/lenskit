/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.util.Iterator;

/**
 * A neighborhood finder that has a snapshot of the rating data for efficiency.  This is built by
 * backing a {@link LiveNeighborFinder} with a {@link org.grouplens.lenskit.data.dao.packed.BinaryRatingDAO}.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
public class SnapshotNeighborFinder implements NeighborFinder {
    private static final Logger logger = LoggerFactory.getLogger(SnapshotNeighborFinder.class);

    private final UserSnapshot snapshot;
    private final UserSimilarity similarity;
    private final UserHistorySummarizer summarizer;
    private final UserVectorNormalizer normalizer;
    private final Threshold threshold;

    @Inject
    public SnapshotNeighborFinder(UserSnapshot snap,
                                  UserSimilarity sim,
                                  UserHistorySummarizer sum,
                                  UserVectorNormalizer norm,
                                  @UserSimilarityThreshold Threshold thresh) {
        snapshot = snap;
        similarity = sim;
        summarizer = sum;
        normalizer = norm;
        threshold = thresh;
    }

    @Override
    public Iterable<Neighbor> getCandidateNeighbors(UserHistory<? extends Event> user, LongSet items) {
        final long uid = user.getUserId();
        SparseVector urs = summarizer.summarize(user);
        final ImmutableSparseVector vector = normalizer.normalize(user.getUserId(), urs, null)
                                                 .freeze();

        LongCollection qset = items;
        if (vector.size() < qset.size()) {
            qset = vector.keySet();
        }
        final LongSet candidates = new LongOpenHashSet();
        for (LongIterator iter = qset.iterator(); iter.hasNext();) {
            final long item = iter.nextLong();
            LongSet users = snapshot.getItemUsers(item);
            if (users != null) {
                candidates.addAll(users);
            }
        }
        candidates.remove(uid);
        logger.debug("Found {} candidate neighbors for user {}", candidates.size(), uid);
        return new Iterable<Neighbor>() {
            @Override
            public Iterator<Neighbor> iterator() {
                return new NeighborIterator(uid, vector, candidates);
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
                SparseVector vector = snapshot.getNormalizedUserVector(neighbor);
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
