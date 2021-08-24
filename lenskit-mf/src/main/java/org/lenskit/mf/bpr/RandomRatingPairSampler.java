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
package org.lenskit.mf.bpr;

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
public class RandomRatingPairSampler implements BPRTrainingSampler {
    private final RatingMatrix snapshot;
    private final Random rand;
    private final int batchSize;
    private final LongList userIds;

    public RandomRatingPairSampler(RatingMatrix snapshot, Random rand) {
    this(snapshot, rand, snapshot.getRatings().size());
    }

    @Inject
    public RandomRatingPairSampler(RatingMatrix snapshot, Random rand, @BatchSize int batchSize) {
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
