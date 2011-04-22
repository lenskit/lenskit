/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval.crossfold;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Iterator;
import java.util.Random;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.Cursors2;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.dao.AbstractRatingDataAccessObject;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * Implementation of a train/test ratings set. It takes care of partitioning the
 * data set into N portions so that each one can be tested against the others.
 * Portions are divided equally, and data is randomized before being
 * partitioned.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CrossfoldManager {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldManager.class);
    private Long2ObjectMap<SparseVector>[] querySets;
    private final int chunkCount;
    private RatingDataAccessObject ratings;

    /**
     * Construct a new train/test ratings set.
     * @param nfolds The number of portions to divide the data set into.
     * @param ratings The ratings data to partition.
     */
    @SuppressWarnings("unchecked")
    public CrossfoldManager(int nfolds, RatingDataAccessObject ratings,
            UserRatingProfileSplitter splitter) {
        logger.debug("Creating rating set with {} folds", nfolds);
        querySets = new Long2ObjectMap[nfolds];
        for (int i = 0; i < nfolds; i++) {
            querySets[i] = new Long2ObjectOpenHashMap<SparseVector>();
        }
        chunkCount = nfolds;
        this.ratings = ratings;

        Random rnd = new Random();
        Cursor<UserRatingProfile> userCursor = null;
        try {
            userCursor = ratings.getUserRatingProfiles();
            int nusers = 0;
            for (UserRatingProfile user: userCursor) {
                int n = rnd.nextInt(nfolds);
                SplitUserRatingProfile sp = splitter.splitProfile(user);
                querySets[n].put(sp.getUserId(), sp.getProbeVector());
                nusers++;
            }
            logger.info("Partitioned {} users into {} folds", nusers, nfolds);
        } finally {
            if (userCursor != null)
                userCursor.close();
        }
    }

    public int getChunkCount() {
        return chunkCount;
    }

    /**
     * Build a training data collection.  The collection is built on-demand, so
     * it doesn't use much excess memory.
     * @param testIndex The index of the test set to use.
     * @return The union of all data partitions except testIndex.
     */
    public RatingDataAccessObject trainingSet(final int testIndex) {
        final Long2ObjectMap<SparseVector> qmap = querySets[testIndex];
        Predicate<Rating> filter = new Predicate<Rating>() {
            public boolean apply(Rating r) {
                SparseVector v = qmap.get(r.getUserId());
                return v == null || !v.containsKey(r.getItemId());
            }
        };
        return new RatingFilteredDAO(ratings, filter);
    }

    /**
     * Return a test data set.
     * @param testIndex The index of the test set to use.
     * @return The test set of users.
     *
     * @todo Fix this method to be more efficient - currently, we convert from
     * vectors to ratings to later be converted back to vectors. That's slow.
     */
    public RatingDataAccessObject testSet(final int testIndex) {
        return new TestDAO(testIndex);
    }
    
    class TestDAO extends AbstractRatingDataAccessObject {
        Long2ObjectMap<SparseVector> queryMap;
        public TestDAO(int testIndex) {
            queryMap = querySets[testIndex];
        }

        @Override
        public LongCursor getUsers() {
            return Cursors2.wrap(queryMap.keySet());
        }
        @Override
        public Cursor<Rating> getRatings() {
            return Cursors.wrap(Iterators.concat(new Iterator<Iterator<Rating>>() {
                Iterator<Long2ObjectMap.Entry<SparseVector>> iter =
                    queryMap.long2ObjectEntrySet().iterator();
                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }
                @Override
                public Iterator<Rating> next() {
                    Long2ObjectMap.Entry<SparseVector> e = iter.next();
                    long uid = e.getLongKey();
                    SparseVector v = e.getValue();
                    return Ratings.fromUserVector(uid, v).iterator();
                }
                public void remove() { throw new UnsupportedOperationException(); }
            }));
        }
    }
}
