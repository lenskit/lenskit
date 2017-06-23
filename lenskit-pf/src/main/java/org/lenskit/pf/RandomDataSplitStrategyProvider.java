package org.lenskit.pf;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.stat.interval.IntervalUtils;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Shareable;
import org.lenskit.inject.Transient;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.KeyIndex;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.inject.Provider;

import java.util.*;


public class RandomDataSplitStrategyProvider implements Provider<DataSplitStrategy> {
    private final RatingMatrix snapshot;
    private final Random random;
    private final double proportion;

    @Inject
    public RandomDataSplitStrategyProvider(@Transient RatingMatrix snapshot,
                                           Random rnd,
                                           @RandomSeed int seed,
                                           @SplitProportion double proportion) {
        this.snapshot = snapshot;
        rnd.setSeed((long)seed);
        random = rnd;
        this.proportion = proportion;
    }


    @Override
    public DataSplitStrategy get() {
        final List<RatingMatrixEntry> allRatings = ImmutableList.copyOf(snapshot.getRatings());
        final int size = allRatings.size();
        final int validationSize = Math.toIntExact(Math.round(size*proportion));
        IntSet randomIndices = new IntOpenHashSet();
        while (randomIndices.size() < validationSize) {
            randomIndices.add(random.nextInt(size));
        }

        List<RatingMatrixEntry> validationRatings = new ArrayList<>();
        Iterator<Integer> iter = randomIndices.iterator();
        while (iter.hasNext()) {
            int index = iter.next();
            validationRatings.add(allRatings.get(index));
        }

        Int2ObjectMap<Int2DoubleMap> trainingRatings = new Int2ObjectOpenHashMap<>();
        Int2ObjectMap<IntSet> userItemIndices = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < size; i++) {
            RatingMatrixEntry e = allRatings.get(i);
            if (!randomIndices.contains(i)) {
                int userIndex = e.getUserIndex();
                int itemIndex = e.getItemIndex();
                double rating = e.getValue();

                Int2DoubleMap itemRatings = trainingRatings.get(itemIndex);
                if (itemRatings == null) itemRatings = new Int2DoubleOpenHashMap();
                itemRatings.put(userIndex, rating);
                trainingRatings.put(itemIndex, itemRatings);

                IntSet userItems = userItemIndices.get(userIndex);
                if (userItems == null) userItems = new IntOpenHashSet();
                userItems.add(itemIndex);
                userItemIndices.put(userIndex, userItems);
            }
        }

        final KeyIndex userIndex = snapshot.userIndex().frozenCopy();
        final KeyIndex itemIndex = snapshot.itemIndex().frozenCopy();
        Int2ObjectMap<ImmutableSet> userItems = new Int2ObjectOpenHashMap<>();

        Iterator<Map.Entry<Integer,IntSet>> userItemsIter = userItemIndices.entrySet().iterator();
        while (userItemsIter.hasNext()) {
            Map.Entry<Integer,IntSet> entry = userItemsIter.next();
            int user = entry.getKey();
            ImmutableSet items = ImmutableSet.copyOf(entry.getValue());
            userItems.put(user, items);
            userItemsIter.remove();
        }

        return new RandomDataSplitStrategy(trainingRatings, ImmutableList.copyOf(validationRatings), userIndex, itemIndex);
    }
}
