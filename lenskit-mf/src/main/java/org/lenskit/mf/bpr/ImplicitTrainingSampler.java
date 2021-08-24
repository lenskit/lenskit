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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.ratings.InteractionEntityType;
import org.lenskit.inject.Transient;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.HashKeyIndex;
import org.lenskit.util.keys.KeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Randomly samples training pairs by selecting a rated/purchased item, and sampling an unrated item for that user.
 */
public class ImplicitTrainingSampler implements BPRTrainingSampler {
    private static final Logger logger = LoggerFactory.getLogger(ImplicitTrainingSampler.class);
    private final RandomGenerator rand;
    private final int batchSize;

    private final KeyIndex userIndex;
    private final KeyIndex itemIndex;
    private final int[][] interactions;
    // store the number of ratings before this user's ratings
    private final int[] cumulativeInteractionCounts;
    private final int totalInteractionCount;

    @Inject
    public ImplicitTrainingSampler(@Transient DataAccessObject dao, @InteractionEntityType EntityType type,
                                   RandomGenerator rand, @BatchSize int batchSize) {
        this.rand = rand;
        this.batchSize = batchSize;

        HashKeyIndex uidx = HashKeyIndex.create();
        HashKeyIndex iidx = HashKeyIndex.create();
        List<IntOpenHashSet> isets = new ArrayList<>();

        logger.debug("scanning interactions");
        try (ObjectStream<Entity> positives = dao.query(type).stream()) {
            for (Entity p: positives) {
                long user = p.getLong(CommonAttributes.USER_ID);
                long item = p.getLong(CommonAttributes.ITEM_ID);
                int uid = uidx.internId(user);
                int iid = iidx.internId(item);
                if (uid >= isets.size()) {
                    assert uid == isets.size();
                    isets.add(new IntOpenHashSet());
                }
                isets.get(uid).add(iid);
            }
        }

        logger.debug("freezing data structures");
        userIndex = uidx.frozenCopy();
        itemIndex = iidx.frozenCopy();

        interactions = new int[isets.size()][];
        cumulativeInteractionCounts = new int[isets.size()];
        int n = 0;
        for (int i = 0; i < isets.size(); i++) {
            IntOpenHashSet ohs = isets.get(i);
            if (ohs.size() > itemIndex.size() / 2) {
                logger.warn("user {} has rated more than half the items, this will slow training pair sampling",
                            userIndex.getKey(i));
            }
            interactions[i] = ohs.toIntArray();
            // sort for fast searching
            Arrays.sort(interactions[i]);
            cumulativeInteractionCounts[i] = n;
            n += ohs.size();
        }
        totalInteractionCount = n;
        logger.info("sampling from {} interactions by {} users",
                    totalInteractionCount, userIndex.size());
    }


    @Override
    public Iterable<? extends TrainingItemPair> nextBatch() {
        return new TrainingBatch();
    }

    public TrainingItemPair nextPair() {
        // we sample a rating index
        int samp = rand.nextInt(totalInteractionCount);
        // now we figure out which user has it
        int pos = Arrays.binarySearch(cumulativeInteractionCounts, samp);
        int uidx;
        if (pos >= 0) {
            // we found the sample: that means it's the first rating for a user
            uidx = pos;
        } else {
            // oops; -pos-1 is the index of the user *after* the one we want
            uidx = -pos - 2;
            assert uidx >= 0 && uidx < cumulativeInteractionCounts.length;
        }
        // the index of this user's first rating
        int ustart = cumulativeInteractionCounts[uidx];
        int[] uitems = interactions[uidx];
        assert samp >= ustart;
        assert samp - ustart < uitems.length;

        int iidx = uitems[samp-ustart];
        if (logger.isTraceEnabled()) {
            logger.trace("sampling for user {}, item {}",
                         userIndex.getKey(uidx), itemIndex.getKey(iidx));
        }

        // rejection-sample an unrated item
        // since user items are sorted, we can do a binary search to see if they rated a candidate j
        int jidx;
        int samples = 0;
        do {
            jidx = rand.nextInt(itemIndex.size());
            samples += 1;
            if (samples > 100) {
                logger.warn("took more than 100 samples to find negative item for user ",
                            userIndex.getKey(uidx));
                samples = Integer.MIN_VALUE; // avoid warning again
            }
        } while (jidx == iidx || Arrays.binarySearch(uitems, jidx) >= 0);

        return new TrainingItemPair(userIndex.getKey(uidx),
                                    itemIndex.getKey(iidx),
                                    itemIndex.getKey(jidx));
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
