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

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Iterator;

/**
 * A neighborhood finder that has a snapshot of the rating data for efficiency.  This is built by
 * backing a {@link LiveNeighborFinder} with a {@link org.lenskit.data.packed.BinaryRatingDAO}.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
public class SnapshotNeighborFinder implements NeighborFinder {
    private static final Logger logger = LoggerFactory.getLogger(SnapshotNeighborFinder.class);

    private final UserSnapshot snapshot;
    private final UserSimilarity similarity;
    private final RatingVectorPDAO rvDAO;
    private final UserVectorNormalizer normalizer;
    private final Threshold threshold;

    @Inject
    public SnapshotNeighborFinder(UserSnapshot snap,
                                  UserSimilarity sim,
                                  RatingVectorPDAO rvd,
                                  UserVectorNormalizer norm,
                                  @UserSimilarityThreshold Threshold thresh) {
        snapshot = snap;
        similarity = sim;
        rvDAO = rvd;
        normalizer = norm;
        threshold = thresh;
    }

    @Override
    public Iterable<Neighbor> getCandidateNeighbors(final long user, LongSet items) {
        SparseVector urs = MutableSparseVector.create(rvDAO.userRatingVector(user));
        if (urs.isEmpty()) {
            return Collections.emptyList();
        }

        final ImmutableSparseVector vector = normalizer.normalize(user, urs, null)
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
        candidates.remove(user);
        logger.debug("Found {} candidate neighbors for user {}", candidates.size(), user);
        return new Iterable<Neighbor>() {
            @Override
            public Iterator<Neighbor> iterator() {
                return new NeighborIterator(user, vector, candidates);
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
