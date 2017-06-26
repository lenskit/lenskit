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

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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

import static org.hamcrest.Matchers.equalTo;
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
        Int2ObjectMap<Int2DoubleMap> trainingRaings = splitStrategy.getTrainingMatrix();
        assertThat(validations.size(), equalTo(3));
        int trainingSize = 0;
        for (int item : trainingRaings.keySet()) {
            trainingSize += trainingRaings.get(item).size();
        }
        assertThat(trainingSize, equalTo(26));
    }

    @Test
    public void testGetRatings() throws Exception {
        RandomDataSplitStrategyProvider splitData = new RandomDataSplitStrategyProvider(snapshot, new Random(), 0, 0.2);
        DataSplitStrategy splitStrategy = splitData.get();
        KeyIndex userIndex = splitStrategy.getUserIndex();
        KeyIndex itemIndex = splitStrategy.getItemIndex();
        List<RatingMatrixEntry> validations = splitStrategy.getValidationRatings();
        Int2ObjectMap<Int2DoubleMap> trainingRatings = splitStrategy.getTrainingMatrix();

        for (int item : trainingRatings.keySet()) {
            for (int user : trainingRatings.get(item).keySet()) {
                long userId = userIndex.getKey(user);
                long itemId = itemIndex.getKey(item);
                double rating = trainingRatings.get(item).get(user);
                assertThat(rating, equalTo(snapshot.getUserRatingVector(userId).get(itemId)));
                assertThat(rating, equalTo(data.get(itemId).get(userId)));
            }
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
        Int2ObjectMap<Int2DoubleMap> trainingRatings = splitStrategy.getTrainingMatrix();

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
        for (int item : trainingRatings.keySet()) {
            for (int user : trainingRatings.get(item).keySet()) {
                long userId = userIndex.getKey(user);
                long itemId = itemIndex.getKey(item);
                assertThat(user, equalTo(snapshotUserIndex.tryGetIndex(userId)));
                assertThat(item, equalTo(snapshotItemIndex.tryGetIndex(itemId)));
            }
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