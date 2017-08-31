/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.pf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.*;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Transient;
import org.lenskit.util.keys.KeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import java.util.*;

/**
 * A provider for {@link RandomDataSplitStrategy}
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RandomDataSplitStrategyProvider implements Provider<DataSplitStrategy> {
    private static Logger logger = LoggerFactory.getLogger(RandomDataSplitStrategyProvider.class);

    private final RatingMatrix snapshot;
    private final Random random;
    private final double proportion;

    /**
     * Construct random data split configuration
     * @param snapshot a snapshot of ratings
     * @param rnd random generator
     * @param seed seed of {@code rnd}
     * @param proportion proportion of validation data
     */
    @Inject
    public RandomDataSplitStrategyProvider(@Transient @Nonnull RatingMatrix snapshot,
                                           Random rnd,
                                           @RandomSeed int seed,
                                           @SplitProportion double proportion) {
        this.snapshot = snapshot;
        rnd.setSeed((long)seed);
        random = rnd;
        this.proportion = proportion;
    }


    @Override
    public RandomDataSplitStrategy get() {
        final int userNum = snapshot.userIndex().size();
        final int itemNum = snapshot.itemIndex().size();
        logger.info("Rating matrix size: {} users and {} items", userNum, itemNum);
        final List<RatingMatrixEntry> allRatings = ImmutableList.copyOf(snapshot.getRatings());
        final int size = allRatings.size();
        final int validationSize = Math.toIntExact(Math.round(size*proportion));
        logger.info("validation set size: {} ratings", validationSize);
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

                if (rating > 0) {
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
        }

        final KeyIndex userIndex = snapshot.userIndex().frozenCopy();
        final KeyIndex itemIndex = snapshot.itemIndex().frozenCopy();
        Int2ObjectMap<ImmutableSet<Integer>> userItems = new Int2ObjectOpenHashMap<>();

        Iterator<Map.Entry<Integer,IntSet>> userItemsIter = userItemIndices.entrySet().iterator();
        while (userItemsIter.hasNext()) {
            Map.Entry<Integer,IntSet> entry = userItemsIter.next();
            int user = entry.getKey();
            IntSet itemInds = entry.getValue();
            ImmutableSet<Integer> items = ImmutableSet.copyOf(itemInds);
            userItems.put(user, items);
            userItemsIter.remove();
        }

        final int exampleItemNum = trainingRatings.keySet().iterator().nextInt();
        logger.info("Training Rating examples: item {} ratings {} ", exampleItemNum, trainingRatings.get(exampleItemNum));
        logger.info("validation ratings {}", validationRatings);
        return new RandomDataSplitStrategy(trainingRatings, ImmutableList.copyOf(validationRatings), userItems, userIndex, itemIndex);
    }
}
