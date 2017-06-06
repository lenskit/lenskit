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
package org.lenskit.mf.BPR;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Random;

/**
 * Randomly generates training pairs by selecting a random user and two random rated items, allowing BPR to be used for
 * ratings (as opposed to only being used for unary data). Rejection sampling is used to generate samples with these properties:
 * If the two random rated items have different ratings, than those items are returned as a training pair.
 * Otherwise, the algorithm will loop until it finds a training pair.
 */
public class RandomRatingPairGenerator implements TrainingPairGenerator {
    private final RatingMatrix snapshot;
    private final Random rand;
    private final int batchSize;
    private final LongList userIds;

    public RandomRatingPairGenerator(RatingMatrix snapshot, Random rand) {
    this(snapshot, rand, snapshot.getRatings().size());
    }

    @Inject
    public RandomRatingPairGenerator(RatingMatrix snapshot, Random rand, @BatchSize int batchSize) {
        this.snapshot = snapshot;
        this.userIds = new LongArrayList(snapshot.getUserIds());
        this.rand = rand;
        this.batchSize = batchSize;
    }


    @Override
    public Iterable<? extends TrainingItemPair> nextBatch() {
        return new TrainingBatch();
    }

    public TrainingItemPair nextPair() {
        TrainingItemPair out = null;
        while(out == null) {
            out = tryGenTrainingPair();
        }
        return out;
    }


    private TrainingItemPair tryGenTrainingPair() {
        // randomly generate training pairs, return

        long userId = userIds.getLong(rand.nextInt(userIds.size()));
        Long2DoubleMap ratings = snapshot.getUserRatingVector(userId);

        long iid, jid;
        int iidx = rand.nextInt(ratings.size());
        int jidx = rand.nextInt(ratings.size());

        if (ratings instanceof Long2DoubleSortedArrayMap) {
            // if we are in the common case and the ratings vector is a L2DSAM we can speed this up significantly.
            Long2DoubleSortedArrayMap sortedRatings = (Long2DoubleSortedArrayMap) ratings;
            iid = sortedRatings.getKeyByIndex(iidx);
            jid = sortedRatings.getKeyByIndex(jidx);
        } else {
            long[] ratedItems = ratings.keySet().toLongArray();
            iid = ratedItems[iidx];
            jid = ratedItems[jidx];
        }

        double irat = ratings.get(iid);
        double jrat = ratings.get(jid);

        if (irat > jrat) {
            return new TrainingItemPair(userId, iid, jid);
        } else if (irat < jrat) {
            return new TrainingItemPair(userId, jid, iid);
        } else {
            return null;
        }
    }

    /**
     * private inner class that will iterate a fixed number of times returning random
     * training pairs.
     */
    private class TrainingBatch implements Iterable<TrainingItemPair>, Iterator<TrainingItemPair> {
        private int remainingPairs = batchSize;

        @Override
        public Iterator<TrainingItemPair> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return remainingPairs>0;
        }

        @Override
        public TrainingItemPair next() {
            remainingPairs = remainingPairs - 1;
            return nextPair();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
