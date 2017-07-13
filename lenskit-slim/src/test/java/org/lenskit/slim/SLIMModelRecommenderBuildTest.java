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
package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.iterative.*;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.*;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;

import org.lenskit.knn.item.MinCommonUsers;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.transform.normalize.*;

import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Vectors;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Build Slim model recommender test.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SLIMModelRecommenderBuildTest {
    private static final int USER_NUM = 200; // number of total user
    private static final int ITEM_NUM = 100; // upper bound of item id (exclusive)
    private Long2DoubleMap y;
    private Long2ObjectMap<Long2DoubleMap> data;
    private Long2DoubleMap weights;
    private DataAccessObject dao;


    /**
     * Create simulated weights which the linear regression tries to learn
     * @param maxItemId upper bound of item Id
     * @param maxWeight max possible value of simulated weight
     * @return simulated weight vector
     */
    static Long2DoubleMap createWeights(long maxItemId, int maxWeight) {
        Long2DoubleMap weights = new Long2DoubleOpenHashMap();
        Random rnd = new Random();
        for (long w = 0; w < maxItemId; w++) {
            int maxW = rnd.nextInt(maxWeight);
            weights.put(w, maxW*rnd.nextDouble());
        }

        return weights;
    }

    /**
     * Simulate row view of user-item ratings matrix (a map of user Ids to user ratings) and label vector whose element satisfies equation (Y = X*W + epsilon) epsilon is a sample of Gaussian Distribution N(0, 1)
     * @param maxItemId max item Id
     * @param weights weight vector used to compute simulated label vector
     * @return user-item ratings matrix whose last row is simulated label y
     */
    static Long2ObjectMap<Long2DoubleMap> createDataModel(int maxItemId, Long2DoubleMap weights) {
        Random rndGen = new Random();
        Long2ObjectMap<Long2DoubleMap> simulatedData = new Long2ObjectOpenHashMap<>();
        Long2DoubleMap labels = new Long2DoubleOpenHashMap();

        int maxRatingNum = 100; // each user's max possible rating number (less than maxItemId)
        int maxUserId = 5000; // greater than USER_NUM (userId not necessarily ranging from 0 to USER_NUM)

        double ratingValue[] = {1, 2, 3, 4, 5};

        int size = 0;
        while (size < USER_NUM) {

            long userId = rndGen.nextInt(maxUserId); // produce random user Id
            int userRatingNum = rndGen.nextInt(maxRatingNum) + 1; // produce random total rating number of a user (non-empty)
//            int userRatingNum = ITEM_NUM;  // non-sparse rating matrix for user rating all items
            Long2DoubleMap userRatings = new Long2DoubleOpenHashMap();
            int i = 0;
            while (i < userRatingNum) {
                long itemId = rndGen.nextInt(maxItemId);
                double rating = ratingValue[rndGen.nextInt(5)];
//                double rating = ratingRange * rndGen.nextDouble();
                userRatings.put(itemId, rating);
                i = userRatings.keySet().size();
            }

            double label = Vectors.dotProduct(userRatings, weights) + rndGen.nextGaussian();
            labels.put(userId, label);
            simulatedData.put(userId, userRatings);
            size = simulatedData.keySet().size();
        }

        simulatedData.put((long)maxUserId, labels); // put simulated label into last row
        return simulatedData;
    }


    private void setup(Long2ObjectMap<Long2DoubleMap> data) throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        Iterator<Map.Entry<Long,Long2DoubleMap>> iter = data.entrySet().iterator();
        while (iter.hasNext()) {

            Map.Entry<Long,Long2DoubleMap> entry = iter.next();
            long itemId = entry.getKey();
            Long2DoubleMap ratings = entry.getValue();
            for (long userId : ratings.keySet()) {
                rs.add(Rating.create(userId, itemId, ratings.get(userId)));
            }
        }

        StaticDataSource source = StaticDataSource.fromList(rs);
        dao = source.get();
    }

    @SuppressWarnings("unchecked")
    private LenskitRecommenderEngine makeEngine() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();

        config.bind(DataAccessObject.class).to(dao);
        config.bind(ItemScorer.class)
                .to(SLIMItemScorer.class);
        config.bind(StoppingCondition.class)
                .to(ThresholdStoppingCondition.class);
//        config.bind(SLIMBuildContext.class)
//                .toProvider(SLIMBuildContextProvider.class);
        config.bind(UserVectorNormalizer.class)
                .to(BaselineSubtractingUserVectorNormalizer.class);
//        config.bind(VectorNormalizer.class)
//                .to(IdentityVectorNormalizer.class);
        config.within(UserVectorNormalizer.class)
                .bind(BaselineScorer.class, ItemScorer.class)
                .to(ItemMeanRatingItemScorer.class);
//        config.set(IterationCount.class)
//                .to(10);
        config.set(ModelSize.class)
                .to(100);
//        config.set(MinCommonUsers.class)
//                .to(2);
        config.set(StoppingThreshold.class)
                .to(1.0e-2);

        return LenskitRecommenderEngine.build(config);
    }

    @Before
    public void buildModel() {
        int maxWeight = 5;
        this.weights = createWeights(ITEM_NUM, maxWeight);
        Long2ObjectMap<Long2DoubleMap> dataWithLabels = new Long2ObjectOpenHashMap<>(createDataModel(ITEM_NUM, weights));
        LongOpenHashBigSet userIdSet = new LongOpenHashBigSet(dataWithLabels.keySet());
        long maxUserId = Collections.max(userIdSet);
        y = new Long2DoubleOpenHashMap(dataWithLabels.get(maxUserId));
        dataWithLabels.remove(maxUserId);
        data = Vectors.transposeMap(dataWithLabels);
        setup(data);
    }


    @Test
    public void testLabels() {
        int sizeOfy = y.size();
        assertThat(sizeOfy, equalTo(USER_NUM));
    }

    @Test
    public void testWeights() {
        int sizeOfWeights = weights.size();
        assertThat(sizeOfWeights, equalTo(ITEM_NUM));
    }

    @Test
    public void testDataSize() {
        Long2ObjectMap<Long2DoubleMap> dataT = Vectors.transposeMap(data);
        int totalUsers = dataT.keySet().size();
        assertThat(totalUsers, equalTo(USER_NUM));
    }


    @Test
    public void testRecommenderBuild() {
        LenskitRecommenderEngine engine = makeEngine();
        try (Recommender rec = engine.createRecommender(dao)) {
            assertThat(rec.getItemScorer(),
                    instanceOf(SLIMItemScorer.class));
            assertThat(rec.getItemRecommender(),
                    instanceOf(TopNItemRecommender.class));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testConfigSeparation() {
        LenskitRecommenderEngine engine = makeEngine();
        try (LenskitRecommender rec1 = engine.createRecommender();
             LenskitRecommender rec2 = engine.createRecommender()) {

            assertThat(rec1.getItemScorer(),
                    not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(SLIMModel.class),
                    allOf(not(nullValue()),
                            sameInstance(rec2.get(SLIMModel.class))));
        }
    }

    @Test
    public void testSLIMScorer() {
        LenskitRecommenderEngine engine = makeEngine();
        Long2ObjectMap<Long2DoubleMap> dataTrans = Vectors.transposeMap(data);

        Iterator<Long> iter = dataTrans.keySet().iterator();
        final double userNum = dataTrans.size();
        double rmseByUser = 0.0;

        while (iter.hasNext()) {
            final long user = iter.next();
            Long2DoubleMap userRatings = dataTrans.get(user);
            try (Recommender rec = engine.createRecommender(dao)) {
                ItemScorer scorer = rec.getItemScorer();
                assertThat(scorer, notNullValue());
                ResultMap details = scorer.scoreWithDetails(user, new ArrayList<>(userRatings.keySet()));
                double rmse = 0.0;
                for (long item : userRatings.keySet()) {
                    Result r = details.get(item);
                    assertThat(r, notNullValue());
                    double actual = userRatings.get(item);
                    double prediction = r.getScore();
                    rmse += Math.pow((actual - prediction),2);
                    System.out.println("user " + user + " item " + item + ": actual rating and prediction: " + actual + "   " + prediction);
                }

                rmse /= userRatings.keySet().size();
                rmse = Math.sqrt(rmse);
                rmseByUser += rmse;
                Map<Long,Double> scoreMap = details.scoreMap();
                System.out.println("user " + user + " actual ratings in test class: ");
                System.out.println(LongUtils.frozenMap(userRatings));
                System.out.println("user " + user + " actual prediction in test class: ");
                System.out.println(LongUtils.frozenMap(scoreMap));
                System.out.println("user " + user + " rmse is: " + rmse);
            }
        }
        rmseByUser /= userNum;
        System.out.println("SLIM recommender RMSE is " + rmseByUser);

    }

    @Test
    public void testRecommender() {
        LenskitRecommenderEngine engine = makeEngine();
        Long2ObjectMap<Long2DoubleMap> dataTrans = Vectors.transposeMap(data);
        Iterator<Long> iter = dataTrans.keySet().iterator();
        long existingUserId = iter.next();
        int notRatedItemSize = ITEM_NUM - dataTrans.get(existingUserId).keySet().size();
        long nonExistingUserId = Collections.max(dataTrans.keySet()) + 1;
        try (Recommender rec = engine.createRecommender(dao)) {
            List<Long> existingUserRec = rec.getItemRecommender().recommend(existingUserId);
            List<Long> nonExistingUserRec = rec.getItemRecommender().recommend(nonExistingUserId);
            assertThat(existingUserRec, hasSize(notRatedItemSize));
            assertThat(nonExistingUserRec, hasSize(ITEM_NUM));
        }

    }

}
