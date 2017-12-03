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
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Transient;
import org.lenskit.util.keys.KeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

        final List<RatingMatrixEntry> validationRatings = ImmutableList.copyOf(subList);
        subList.clear();
        logger.info("validation rating size: {}", validationRatings.size());

        final KeyIndex userIndex = snapshot.userIndex();
        final KeyIndex itemIndex = snapshot.itemIndex();

        return new RandomDataSplitStrategy(allRatings, validationRatings, userIndex, itemIndex);
    }
}
