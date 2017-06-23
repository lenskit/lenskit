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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class RandomDataSplitStrategyProviderTest {
    private Long2ObjectMap<Long2DoubleMap> data;
    private RatingMatrix snapshot;

    @Before
    public void setUp() throws Exception {
        List<Rating> rs = new ArrayList<>();
        double[][] ratings = {
                {11, 12, 13, 14, 15},
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
        List<RatingMatrixEntry> validations = splitData.getValidationRatings();
        List<RatingMatrixEntry> trainings = splitData.getTrainingRatings();
        assertThat(validations.size(), equalTo(3));
        assertThat(trainings.size(), equalTo(27));
    }

    @Test
    public void testGetRatings() throws Exception {
        RandomDataSplitStrategyProvider splitData = new RandomDataSplitStrategyProvider(snapshot, new Random(), 0, 0.2);
        List<RatingMatrixEntry> validations = splitData.getValidationRatings();
        List<RatingMatrixEntry> trainingRatings = splitData.getTrainingRatings();

        for (RatingMatrixEntry re : trainingRatings) {
            long userId = re.getUserId();
            long itemId = re.getItemId();
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
        KeyIndex userIndex = splitData.getUserIndex();
        KeyIndex itemIndex = splitData.getItemIndex();
        List<RatingMatrixEntry> validations = splitData.getValidationRatings();
        for (RatingMatrixEntry re : validations) {
            int user = re.getUserIndex();
            int item = re.getItemIndex();
            long userId = re.getUserId();
            long itemId = re.getItemId();
            assertThat(user, equalTo(userIndex.tryGetIndex(userId)));
            assertThat(item, equalTo(itemIndex.tryGetIndex(itemId)));
        }

        List<RatingMatrixEntry> trainingRatings = splitData.getTrainingRatings();
        for (RatingMatrixEntry re : trainingRatings) {
            int user = re.getUserIndex();
            int item = re.getItemIndex();
            long userId = re.getUserId();
            long itemId = re.getItemId();
            assertThat(user, equalTo(userIndex.tryGetIndex(userId)));
            assertThat(item, equalTo(itemIndex.tryGetIndex(itemId)));
        }

        KeyIndex snapshotUserIndex = snapshot.userIndex();
        KeyIndex snapshotItemIndex = snapshot.itemIndex();
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