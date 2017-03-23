package org.lenskit.mf.BPR;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.lenskit.data.ratings.RatingMatrix;

import javax.inject.Inject;
import java.util.Random;

/**
 * Created by Kluver on 8/22/15.
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
        for (int i = 0; i < 100; i++) {
            out = tryGenTrainingPair();
            if (out != null) {
                break;
            }
        }

        return out;
    }

    private TrainingItemPair tryGenTrainingPair() {
        // not necissarily efficient, but hey, it works!

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
