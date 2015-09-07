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
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.ratings.Rating;

import java.util.Random;

class UserSampleCrossfoldMethod extends UserBasedCrossfoldMethod {
    final int sampleSize;

    UserSampleCrossfoldMethod(SortOrder ord, HistoryPartitionMethod part, int size) {
        super(ord, part);
        sampleSize = size;
    }

    @Override
    protected Long2IntMap splitUsers(LongSet users, int np, Random rng) {
        if (np * sampleSize > users.size()) {
            logger.warn("cannot make {} disjoint samples of {} from {} users, partitioning",
                        np, sampleSize, users.size());
            UserPartitionCrossfoldMethod upcm = new UserPartitionCrossfoldMethod(order, partition);
            return upcm.splitUsers(users, np, rng);
        } else {
            Long2IntMap userMap = new Long2IntOpenHashMap(users.size());
            logger.info("Sampling {} users into {} disjoint samples of {}",
                        users.size(), np, sampleSize);
            long[] userArray = users.toLongArray();
            LongArrays.shuffle(userArray, rng);
            for (int p = 0; p < np; p++) {
                for (int i = 0; i < sampleSize; i++) {
                    long u = userArray[p*sampleSize + i];
                    userMap.put(u, p);
                }
            }
            return userMap;
        }
    }
}
