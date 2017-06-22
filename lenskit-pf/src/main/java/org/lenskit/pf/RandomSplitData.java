package org.lenskit.pf;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.KeyIndex;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Shareable
@Immutable
public class RandomSplitData implements DataSplitStrategy, Serializable {
    private final List<RatingMatrixEntry> allRatings;
    private final ImmutableSet<Integer> indicesOfRatings;
    private final KeyIndex userIndex;
    private final KeyIndex itemIndex;

    @Inject
    public RandomSplitData(RatingMatrix snapshot,
                           Random rnd,
                           @RandomSeed int seed,
                           @SplitProportion double proportion) {
        rnd.setSeed((long)seed);
        userIndex = snapshot.userIndex().frozenCopy();
        itemIndex = snapshot.itemIndex().frozenCopy();
        allRatings = ImmutableList.copyOf(snapshot.getRatings());
        int size = allRatings.size();
        int validationSize = Math.toIntExact(Math.round(size*proportion));
        IntSet randomIndices = new IntOpenHashSet();
        while (randomIndices.size() < validationSize) {
            randomIndices.add(rnd.nextInt(size));
        }
        indicesOfRatings = ImmutableSet.copyOf(randomIndices);
    }

    @Override
    public List<RatingMatrixEntry> getTrainingRatings() {
        List<RatingMatrixEntry> trainingRatings = new ArrayList<>();

        for (int i = 0; i < allRatings.size(); i++) {
            RatingMatrixEntry e = allRatings.get(i);
            if (!indicesOfRatings.contains(i)) trainingRatings.add(e);
        }
        return ImmutableList.copyOf(trainingRatings);
    }

    @Override
    public List<RatingMatrixEntry> getValidationRatings() {
        List<RatingMatrixEntry> validationRatings = new ArrayList<>();
        Iterator<Integer> iter = indicesOfRatings.iterator();
        while (iter.hasNext()) {
            int index = iter.next();
            validationRatings.add(allRatings.get(index));
        }
        return ImmutableList.copyOf(validationRatings);
    }

    @Override
    public KeyIndex userIndex() {
        return userIndex;
    }

    @Override
    public KeyIndex itemIndex() {
        return itemIndex;
    }
}
