/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
        List<RatingMatrixEntry> allRatings = new ArrayList<>(snapshot.getRatings());
        final int size = allRatings.size();
        final int validationSize = Math.toIntExact(Math.round(size*proportion));
        logger.info("validation set size: {} ratings", validationSize);
        Collections.shuffle(allRatings, random);
        List<RatingMatrixEntry> subList = allRatings.subList(0, validationSize);
//        IntSet randomIndices = new IntOpenHashSet();
//        while (randomIndices.size() < validationSize) {
//            randomIndices.add(random.nextInt(size));
//        }
//
//        List<RatingMatrixEntry> validationRatings = new ArrayList<>();
//        Iterator<Integer> iter = randomIndices.iterator();
//        while (iter.hasNext()) {
//            int index = iter.next();
//            validationRatings.add(allRatings.get(index));
//        }

        final List<RatingMatrixEntry> validationRatings = ImmutableList.copyOf(subList);
        subList.clear();
        logger.info("validation rating size: {}", validationRatings.size());
//        Int2ObjectMap<Int2DoubleMap> trainingRatings = new Int2ObjectOpenHashMap<>();
//        Int2ObjectMap<IntSet> userItemIndices = new Int2ObjectOpenHashMap<>();
//        Iterator<RatingMatrixEntry> ratingIter = allRatings.iterator();
//        while (ratingIter.hasNext()) {
//            RatingMatrixEntry e = ratingIter.next();
//            int userIndex = e.getUserIndex();
//            int itemIndex = e.getItemIndex();
//            double rating = e.getValue();
//
//            if (rating > 0) {
//                Int2DoubleMap itemRatings = trainingRatings.get(itemIndex);
//                if (itemRatings == null) itemRatings = new Int2DoubleOpenHashMap();
//                itemRatings.put(userIndex, rating);
//                trainingRatings.put(itemIndex, itemRatings);

//                IntSet userItems = userItemIndices.get(userIndex);
//                if (userItems == null) userItems = new IntOpenHashSet();
//                userItems.add(itemIndex);
//                userItemIndices.put(userIndex, userItems);
//            }
//
//        }
//        for (int i = 0; i < size; i++) {
//            RatingMatrixEntry e = allRatings.get(i);
//            if (!randomIndices.contains(i)) {
//                int userIndex = e.getUserIndex();
//                int itemIndex = e.getItemIndex();
//                double rating = e.getValue();
//
//                if (rating > 0) {
//                    Int2DoubleMap itemRatings = trainingRatings.get(itemIndex);
//                    if (itemRatings == null) itemRatings = new Int2DoubleOpenHashMap();
//                    itemRatings.put(userIndex, rating);
//                    trainingRatings.put(itemIndex, itemRatings);
//
//                    IntSet userItems = userItemIndices.get(userIndex);
//                    if (userItems == null) userItems = new IntOpenHashSet();
//                    userItems.add(itemIndex);
//                    userItemIndices.put(userIndex, userItems);
//                }
//            }
//        }

        final KeyIndex userIndex = snapshot.userIndex();
        final KeyIndex itemIndex = snapshot.itemIndex();
//        Int2ObjectMap<ImmutableSet<Integer>> userItems = new Int2ObjectOpenHashMap<>();
//
//        Iterator<Map.Entry<Integer,IntSet>> userItemsIter = userItemIndices.entrySet().iterator();
//        while (userItemsIter.hasNext()) {
//            Map.Entry<Integer,IntSet> entry = userItemsIter.next();
//            int user = entry.getKey();
//            IntSet itemInds = entry.getValue();
//            ImmutableSet<Integer> items = ImmutableSet.copyOf(itemInds);
//            userItems.put(user, items);
//            userItemsIter.remove();
//        }

//        final int exampleItemNum = trainingRatings.keySet().iterator().nextInt();
//        logger.info("Training Rating examples: item {} ratings {} ", exampleItemNum, trainingRatings.get(exampleItemNum));
//        logger.info("validation ratings {}", validationRatings);

        return new RandomDataSplitStrategy(allRatings, validationRatings, userIndex, itemIndex);
    }
}
