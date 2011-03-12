/*
 * RefLens, a reference implementation of recommender algorithms.
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

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.Collection;
import java.util.Random;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.RatingDataSource;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

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
	private Long2IntMap userPartitionMap;
	private final int chunkCount;
	private RatingDataSource ratings;

	/**
	 * Construct a new train/test ratings set.
	 * @param nfolds The number of portions to divide the data set into.
	 * @param ratings The ratings data to partition.
	 */
	public CrossfoldManager(int nfolds, RatingDataSource ratings) {
		logger.debug("Creating rating set with {} folds", nfolds);
		userPartitionMap = new Long2IntOpenHashMap();
		userPartitionMap.defaultReturnValue(nfolds);
		chunkCount = nfolds;
		this.ratings = ratings;

		Random splitter = new Random();
		Cursor<Long> userCursor = ratings.getUsers();
		try {
			int nusers = userCursor.getRowCount();
			if (nusers >= 0) {
				userPartitionMap = new Long2IntOpenHashMap(nusers);
			} else {
				userPartitionMap = new Long2IntOpenHashMap();
			}
			for (long uid: userCursor) {
				userPartitionMap.put(uid, splitter.nextInt(nfolds));
			}
			logger.info("Partitioned {} users into {} folds", userPartitionMap.size(), nfolds);
		} finally {
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
	public RatingDataSource trainingSet(final int testIndex) {
		Predicate<Long> filter = new Predicate<Long>() {
			public boolean apply(Long uid) {
				return userPartitionMap.get(uid.longValue()) != testIndex;
			}
		};
		return new UserFilteredDataSource(ratings, false, filter);
	}

	public void close() {
		ratings.close();
	}

	/**
	 * Return a test data set.
	 * @param testIndex The index of the test set to use.
	 * @return The test set of users.
	 */
	public Collection<UserRatingProfile> testSet(final int testIndex) {
		Predicate<UserRatingProfile> filter = new Predicate<UserRatingProfile>() {
			public boolean apply(UserRatingProfile profile) {
				int part = userPartitionMap.get(profile.getUser());
				return part == testIndex;
			}
		};
		return Cursors.makeList(org.grouplens.common.cursors.Cursors.filter(ratings.getUserRatingProfiles(), filter));
	}
}
