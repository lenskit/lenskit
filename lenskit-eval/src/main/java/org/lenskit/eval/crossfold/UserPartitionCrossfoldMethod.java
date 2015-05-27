package org.lenskit.eval.crossfold;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.data.event.Rating;

import java.util.Random;

class UserPartitionCrossfoldMethod extends UserBasedCrossfoldMethod {
    public UserPartitionCrossfoldMethod(Order<Rating> ord, PartitionAlgorithm<Rating> pa) {
        super(ord, pa);
    }

    @Override
    protected Long2IntMap splitUsers(LongSet users, int np, Random rng) {
        Long2IntMap userMap = new Long2IntOpenHashMap(users.size());
        logger.info("Splitting {} users into {} partitions", users.size(), np);
        long[] userArray = users.toLongArray();
        LongArrays.shuffle(userArray, rng);
        for (int i = 0; i < userArray.length; i++) {
            final long user = userArray[i];
            userMap.put(user, i % np);
        }
        return userMap;
    }
}
