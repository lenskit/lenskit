package org.lenskit.eval.crossfold;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.data.event.Rating;

import java.util.Random;

class UserSampleCrossfoldMethod extends UserBasedCrossfoldMethod {
    final int sampleSize;

    UserSampleCrossfoldMethod(Order<Rating> ord, PartitionAlgorithm<Rating> part, int size) {
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
                    userMap.put(userArray[i+p], p);
                }
            }
            return userMap;
        }
    }
}
