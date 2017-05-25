package org.lenskit.mf.BPR;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.lenskit.data.ratings.RatingMatrix;

import javax.inject.Inject;
import java.util.Random;

/**
 * Randomly generates training pairs by selecting a random user and two random rated items.
 * If the two random rated items have different ratings, than those items are returned as a training pair.
 * Otherwise, the algorithm will loop until it finds a training pair.
 */
public class RandomRatingPairGenerator implements TrainingPairGenerator{
    private final RatingMatrix snapshot;
    private final Random rand;
    private LongList userIds;

    @Inject
    public RandomRatingPairGenerator(RatingMatrix snapshot, Random rand) {
        this.snapshot = snapshot;
        this.userIds = new LongArrayList(snapshot.getUserIds());
        this.rand = rand;
    }

    @Override
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
        long[] ratedItems = ratings.keySet().toLongArray();

        // item i
        int iidx = rand.nextInt(ratedItems.length);
        long iid = ratedItems[iidx];
        double irat = ratings.get(iid);

        // item j
        int jidx = rand.nextInt(ratedItems.length);
        long jid = ratedItems[jidx];
        double jrat = ratings.get(jid);

        if (irat > jrat) {
            return new TrainingItemPair(userId, iid, jid);
        } else if (irat < jrat ) {
            return new TrainingItemPair(userId, jid, iid);
        } else {
            return null;
        }

    }
}
