/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.eval.crossfold;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.cursors.Cursor;
import org.lenskit.data.ratings.Rating;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.source.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

abstract class UserBasedSplitMethod implements SplitMethod {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Order<Rating> order;
    protected final PartitionAlgorithm<Rating> partition;

    UserBasedSplitMethod(Order<Rating> ord, PartitionAlgorithm<Rating> pa) {
        order = ord;
        partition = pa;
    }

    public void crossfold(DataSource input, CrossfoldOutput output) throws IOException {
        final int count = output.getCount();
        logger.info("splitting data source {} to {} partitions by users",
                    input.getName(), count);
        Long2IntMap splits = splitUsers(input.getUserDAO().getUserIds(), count, output.getRandom());
        splits.defaultReturnValue(-1); // unpartitioned users should only be trained
        Cursor<UserHistory<Rating>> historyCursor = input.getUserEventDAO().streamEventsByUser(Rating.class);

        try {
            for (UserHistory<Rating> history : historyCursor) {
                int foldNum = splits.get(history.getUserId());
                List<Rating> ratings = new ArrayList<Rating>(history);
                final int n = ratings.size();

                for (int f = 0; f < count; f++) {
                    if (f == foldNum) {
                        order.apply(ratings, output.getRandom());
                        final int p = partition.partition(ratings);
                        for (int j = 0; j < p; j++) {
                            output.getTrainWriter(f).writeRating(ratings.get(j));
                        }
                        for (int j = p; j < n; j++) {
                            output.getTestWriter(f).writeRating(ratings.get(j));
                        }
                    } else {
                        for (Rating rating : ratings) {
                            output.getTrainWriter(f).writeRating(rating);
                        }
                    }
                }

            }
        } finally {
            historyCursor.close();
        }
    }

    /**
     * Assign users to partitions.
     * @param users The users to partition.
     * @param np The number of user sets to build.
     * @param rng The random number generator.
     * @return A mapping of users to their test partitions.
     */
    protected abstract Long2IntMap splitUsers(LongSet users, int np, Random rng);
}
