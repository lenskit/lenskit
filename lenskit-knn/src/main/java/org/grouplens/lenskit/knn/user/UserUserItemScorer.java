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

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.VectorTransformation;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

import static java.lang.Math.abs;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class UserUserItemScorer extends AbstractItemScorer {
    private static final double MINIMUM_SIMILARITY = 0.001;
    private static final Logger logger = LoggerFactory.getLogger(UserUserItemScorer.class);
    protected final NeighborhoodFinder neighborhoodFinder;
    protected final UserVectorNormalizer normalizer;

    @Inject
    public UserUserItemScorer(DataAccessObject dao, NeighborhoodFinder nbrf,
                              UserVectorNormalizer norm) {
        super(dao);
        neighborhoodFinder = nbrf;
        normalizer = norm;
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
    public void score(@Nonnull UserHistory<? extends Event> history,
                      @Nonnull MutableSparseVector scores) {
        logger.trace("Predicting for user {} with {} events",
                     history.getUserId(), history.size());

        Long2ObjectMap<? extends Collection<Neighbor>> neighborhoods =
                neighborhoodFinder.findNeighbors(history, scores.keyDomain());
        Long2ObjectMap<SparseVector> normedUsers =
                normalizeNeighborRatings(neighborhoods.values());

        int nmissing = 0;
        for (VectorEntry e : scores.fast(VectorEntry.State.EITHER)) {
            final long item = e.getKey();
            double sum = 0;
            double weight = 0;
            Collection<Neighbor> nbrs = neighborhoods.get(item);
            if (nbrs != null) {
                for (Neighbor n : nbrs) {
                    weight += abs(n.similarity);
                    sum += n.similarity * normedUsers.get(n.user).get(item);
                }
            }

            if (weight >= MINIMUM_SIMILARITY) {
                logger.trace("Total neighbor weight for item {} is {}", item, weight);
                scores.set(e, sum / weight);
            } else {
                nmissing = 0;
                scores.clear(e);
            }
        }

        // Denormalize and return the results
        SparseVector urv = RatingVectorUserHistorySummarizer.makeRatingVector(history);
        VectorTransformation vo = normalizer.makeTransformation(history.getUserId(), urv);
        vo.unapply(scores);
    }
}
