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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.ratings.*;
import org.lenskit.util.keys.KeyIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class RandomDataSplitStrategyProviderTest {
    private Long2ObjectMap<Long2DoubleMap> data;
    private RatingMatrix snapshot;

    @Before
    public void setUp() throws Exception {
        List<Rating> rs = new ArrayList<>();
        double[][] ratings = {
                {0, 12, 13, 14, 15},
                {21, 22, 23, 24, 25},
                {31, 32, 33, 34, 35},
                {41, 42, 43, 44, 45},
                {51, 52, 53, 54, 55},
                {61, 62, 63, 64, 65}};
        EntityFactory ef = new EntityFactory();
        data = new Long2ObjectOpenHashMap<>();
        for (int user = 1; user <= ratings.length; user++) {
            double[] userRatings = ratings[user-1];
            for (int item = 1; item <= userRatings.length; item++) {
                double rating = userRatings[item-1];
                rs.add(ef.rating(user, item, rating));
                Long2DoubleMap itemRatings = data.get(item);
                if (itemRatings == null) itemRatings = new Long2DoubleOpenHashMap();
                itemRatings.put(user, rating);
                data.put(item, itemRatings);
            }
        }

        StaticDataSource source = StaticDataSource.fromList(rs);
        DataAccessObject dao = source.get();
        snapshot = new PackedRatingMatrixProvider(new StandardRatingVectorPDAO(dao), new Random()).get();
    }


    @Test
    public void testDataSize() {
        RandomDataSplitStrategyProvider splitData = new RandomDataSplitStrategyProvider(snapshot, new Random(), 0, 0.1);
        DataSplitStrategy splitStrategy = splitData.get();
        List<RatingMatrixEntry> validations = splitStrategy.getValidationRatings();
        List<RatingMatrixEntry> trainingRaings = splitStrategy.getTrainRatings();
        assertThat(validations.size(), equalTo(3));
        int trainingSize = trainingRaings.size();
        assertThat(trainingSize, equalTo(27));
    }

    @Test
    public void testGetRatings() throws Exception {
        RandomDataSplitStrategyProvider splitData = new RandomDataSplitStrategyProvider(snapshot, new Random(), 0, 0.2);
        DataSplitStrategy splitStrategy = splitData.get();
        KeyIndex userIndex = splitStrategy.getUserIndex();
        KeyIndex itemIndex = splitStrategy.getItemIndex();
        List<RatingMatrixEntry> validations = splitStrategy.getValidationRatings();
        List<RatingMatrixEntry> trainingRatings = splitStrategy.getTrainRatings();

        for (RatingMatrixEntry re : trainingRatings) {
            int user = re.getUserIndex();
            int item = re.getItemIndex();
            long userId = userIndex.getKey(user);
            long itemId = itemIndex.getKey(item);
            double rating = re.getValue();
            assertThat(rating, equalTo(snapshot.getUserRatingVector(userId).get(itemId)));
            assertThat(rating, equalTo(data.get(itemId).get(userId)));

        }

        for (RatingMatrixEntry re : validations) {
            long userId = re.getUserId();
            long itemId = re.getItemId();
            double rating = re.getValue();
            assertThat(rating, equalTo(snapshot.getUserRatingVector(userId).get(itemId)));
            assertThat(rating, equalTo(data.get(itemId).get(userId)));
        }

    }


    @Test
    public void testGetIndex() {
        RandomDataSplitStrategyProvider splitData = new RandomDataSplitStrategyProvider(snapshot, new Random(), 0, 0.1);
        DataSplitStrategy splitStrategy = splitData.get();
        KeyIndex userIndex = splitStrategy.getUserIndex();
        KeyIndex itemIndex = splitStrategy.getItemIndex();
        List<RatingMatrixEntry> validations = splitStrategy.getValidationRatings();
        List<RatingMatrixEntry> trainingRatings = splitStrategy.getTrainRatings();

        for (RatingMatrixEntry re : validations) {
            int user = re.getUserIndex();
            int item = re.getItemIndex();
            long userId = re.getUserId();
            long itemId = re.getItemId();
            assertThat(user, equalTo(userIndex.tryGetIndex(userId)));
            assertThat(item, equalTo(itemIndex.tryGetIndex(itemId)));
        }


        KeyIndex snapshotUserIndex = snapshot.userIndex();
        KeyIndex snapshotItemIndex = snapshot.itemIndex();
        for (RatingMatrixEntry re : trainingRatings) {
            int user = re.getUserIndex();
            int item = re.getItemIndex();
            long userId = userIndex.getKey(user);
            long itemId = itemIndex.getKey(item);
            assertThat(user, equalTo(snapshotUserIndex.tryGetIndex(userId)));
            assertThat(item, equalTo(snapshotItemIndex.tryGetIndex(itemId)));

        }


        int size = snapshotUserIndex.getUpperBound();
        for (int i = 0; i < size; i++) {
            assertThat(snapshotUserIndex.getKey(i), equalTo(userIndex.getKey(i)));
        }

        size = snapshotItemIndex.getUpperBound();
        for (int i = 0; i < size; i++) {
            assertThat(snapshotItemIndex.getKey(i), equalTo(itemIndex.getKey(i)));
        }
    }

}