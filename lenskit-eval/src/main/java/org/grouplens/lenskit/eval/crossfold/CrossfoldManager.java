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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.BasicUserRatingProfile;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
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
        return new RatingFilteredDAO(ratings, querySets[testIndex]);
    }

    /**
     * Return a test data set.
     * @param testIndex The index of the test set to use.
     * @return The test set of users.
     *
     * @todo Fix this method to be more efficient - currently, we convert from
     * vectors to ratings to later be converted back to vectors. That's slow.
     */
    public Collection<UserRatingProfile> testSet(final int testIndex) {
        return new AbstractCollection<UserRatingProfile>() {
            public int size() {
                return querySets[testIndex].size();
            }
            
            public Iterator<UserRatingProfile> iterator() {
                return Iterators.transform(querySets[testIndex].long2ObjectEntrySet().iterator(),
                                           new Function<Long2ObjectMap.Entry<SparseVector>, UserRatingProfile>() {
                    public UserRatingProfile apply(Long2ObjectMap.Entry<SparseVector> entry) {
                        long uid = entry.getLongKey();
                        SparseVector v = entry.getValue();
                        List<Rating> ratings = new ArrayList<Rating>(v.size());
                        for (Long2DoubleMap.Entry e: v.fast()) {
                            ratings.add(new SimpleRating(uid, e.getLongKey(), e.getDoubleValue()));
                        }
                        return new BasicUserRatingProfile(uid, ratings);
                    }
                });
            }
        };
    }
}
