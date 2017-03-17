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
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.IterationCountStoppingCondition;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 * Build Slim model test.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SLIMModelRecommenderBuildTest {
    private Long2DoubleMap y;
    private Long2ObjectMap<Long2DoubleMap> data;
    private Long2DoubleMap weights;
    final static Logger logger = LoggerFactory.getLogger(SLIMModelRecommenderBuildTest.class);
    private DataAccessObject dao;

    /**
     * create simulated weights which the linear regression tries to learn
     * @param maxItemId max value of item Id
     * @param maxWeight max value of simulated weight
     * @return simulated weight vector
     */
    static Long2DoubleMap createWeights(long maxItemId, int maxWeight) {
        Long2DoubleMap weights = new Long2DoubleOpenHashMap();
        Random rnd = new Random();
        for (long w = 0; w < maxItemId; w++) {
            int maxW = rnd.nextInt(maxWeight);
            weights.put(w, maxW*rnd.nextDouble());
        }
        //logger.info("original weights is {} and size is {}", weights, weights.size());
        return weights;
    }

    /**
     * simulate row view of user-item ratings matrix (a map of user Ids to user ratings) and label vector whose element satisfies equation (Y = X*W + epsilon) epsilon is a sample of Gaussian Distribution N(0, 1)
     * @param maxItemId max item Id
     * @param weights weight vector used to compute simulated label vector
     * @return user-item ratings matrix whose last row is simulated label y
     */
    static Long2ObjectMap<Long2DoubleMap> createDataModel(int maxItemId, Long2DoubleMap weights) {
        Random rndGen = new Random();
        Long2ObjectMap<Long2DoubleMap> simulatedData = new Long2ObjectOpenHashMap<>();
        Long2DoubleMap labels = new Long2DoubleOpenHashMap();

        int userNum = 20; // number of total user
        int maxRatingNum = 10; // each user's max possible rating number (less than maxItemId)
        int maxUserId = 5000; // greater than userNum (userId not necessarily ranging from 0 to userNum)
        double ratingRange = 5.0;

        int size = 0;
        while (size < userNum) {

            long userId = rndGen.nextInt(maxUserId); // produce random user Id
            int userRatingNum = rndGen.nextInt(maxRatingNum); // produce random total rating number of a user's
            Long2DoubleMap userRatings = new Long2DoubleOpenHashMap();
            int i = 0;
            while (i < userRatingNum) {
                long itemId = rndGen.nextInt(maxItemId);
                double rating = ratingRange * rndGen.nextDouble();
                userRatings.put(itemId, rating);
                i = userRatings.keySet().size();
            }

            double label = Vectors.dotProduct(userRatings, weights) + rndGen.nextGaussian();
            labels.put(userId, label);
            simulatedData.put(userId, userRatings);
            size = simulatedData.keySet().size();
        }
        simulatedData.put((long)maxUserId, labels); // put simulated label into last row
        //logger.info("rating matrix is {}", simulatedData);
        return simulatedData;
    }


    public void setup(Long2ObjectMap<Long2DoubleMap> data) throws RecommenderBuildException {
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

    private LenskitRecommenderEngine makeEngine() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();

        config.bind(ItemScorer.class)
                .to(SLIMScorer.class);

        config.bind(StoppingCondition.class)
                .to(IterationCountStoppingCondition.class);
        config.set(IterationCount.class)
                .to(10);
        config.set(ModelSize.class)
                .to(20);

        return LenskitRecommenderEngine.build(config, dao);
    }

    @Before
    public void buildModel() {
        int itemNum = 10;
        int maxWeight = 5;
        this.weights = createWeights(itemNum, maxWeight);
        Long2ObjectMap<Long2DoubleMap> dataWithLabels = new Long2ObjectOpenHashMap<>(createDataModel(itemNum, weights));
        LongOpenHashBigSet userIdSet = new LongOpenHashBigSet(dataWithLabels.keySet());
        long maxUserId = Collections.max(userIdSet);
        y = new Long2DoubleOpenHashMap(dataWithLabels.get(maxUserId));
        dataWithLabels.remove(maxUserId);
        data = LinearRegressionHelper.transposeMap(dataWithLabels);
        setup(data);
        LongOpenHashBigSet itemKeySet = new LongOpenHashBigSet(data.keySet());
        logger.info("item matrix size {} \n max item id is {} \n min item id is {} ",itemKeySet.size64(), Collections.max(itemKeySet), Collections.min(itemKeySet));
    }


    @Test
    public void testLabels() {
        int sizeOfy = y.size();
        assertThat(sizeOfy, equalTo(20));
    }

    @Test
    public void testWeights() {
        int sizeOfWeights = weights.size();
        assertThat(sizeOfWeights, equalTo(10));
    }

    @Test
    public void testData() {
        int sizeOfData = data.keySet().size();
        assertThat(sizeOfData, equalTo(10));
    }

    @Test
    public void testLinearRegression() {

        // Naive update
        final long startTimeNaive = System.currentTimeMillis();
        SLIMUpdateParameters parameters = new SLIMUpdateParameters(3, 0.2, false, new IterationCountStoppingCondition(10));
        NaiveUpdate model = new NaiveUpdate(parameters);
        Long2DoubleMap predictedW = model.fit(y, data);
        //Long2DoubleMap predictions = model.predict(data, predictedW);
        Long2DoubleMap residuals = model.computeResiduals(y, data, predictedW);
        //double lossFun = model.computeLossFunction(residuals, predictedW);
        //System.out.println(lossFun);
        final long endTimeNaive = System.currentTimeMillis();


        // Covariance update
        final long startTimeCov = System.currentTimeMillis();
        CovarianceUpdate model_Cov = new CovarianceUpdate(parameters);
        Long2DoubleMap predictedW_Cov = model_Cov.fit(y, data);
        //Long2DoubleMap predictions = model.predict(data, predictedW);
        Long2DoubleMap residuals_Cov = model_Cov.computeResiduals(y, data, predictedW_Cov);
        //double lossFun_Cov = model_Cov.computeLossFunction(residuals_Cov, predictedW_Cov);
        //System.out.println(lossFun);
        final long endTimeCov = System.currentTimeMillis();
        logger.info("running time naive {}", (endTimeNaive - startTimeNaive));
        logger.info("running time Cov {}", (endTimeCov - startTimeCov));


        logger.info("Naive predicted weights is {}", predictedW);
        Long2DoubleMap weightDiff = LinearRegressionHelper.addVectors(weights, Vectors.multiplyScalar(predictedW, -1.0));
        double weightDiffNorm = Vectors.euclideanNorm(weightDiff);
        logger.info("weight difference is {} \n and the norm is {}", weightDiff, weightDiffNorm);

        logger.info("Cov predicted weights is {} \n original weight is {}", predictedW_Cov, weights);
        Long2DoubleMap weightDiff_Cov = LinearRegressionHelper.addVectors(weights, Vectors.multiplyScalar(predictedW_Cov, -1));
        double weightDiffNorm_Cov = Vectors.euclideanNorm(weightDiff_Cov);
        logger.info("Cov update : weight difference is {} \n and the norm is {}", weightDiff_Cov, weightDiffNorm_Cov);
        Long2DoubleMap resOrig = model_Cov.computeResiduals(y, data, weights);
        double lossFunOrig = model_Cov.computeLossFunction(resOrig, weights);
        logger.info("Original loss function is {}", lossFunOrig);
        Long2DoubleMap resAfter = model_Cov.computeResiduals(y, data, predictedW_Cov);
        double lossFunAfter = model_Cov.computeLossFunction(resAfter, predictedW_Cov);
        logger.info("after loss function is {}", lossFunAfter);
        Long2DoubleMap nonzeroWeigt = LinearRegressionHelper.filterValues(predictedW_Cov, 0.0);
        logger.info("non-zero weight size {}", nonzeroWeigt.size());

    }

    @Test
    public void testRecommender() {
        LenskitRecommenderEngine engine = makeEngine();
        try (Recommender rec = engine.createRecommender(dao)) {
            assertThat(rec.getItemScorer(),
                    instanceOf(SLIMScorer.class));
            assertThat(rec.getItemRecommender(),
                    instanceOf(TopNItemRecommender.class));
            Map<Long,Long2DoubleMap> dataTrans = LinearRegressionHelper.transposeMap(data);
            Iterator<Long> iter = dataTrans.keySet().iterator();
            long user = iter.next();
            rec.getItemScorer().scoreWithDetails(user, new ArrayList<>(data.keySet()));
            List<Long> recommend = rec.getItemRecommender().recommend(user);
            logger.info("{}", recommend);
            logger.info("input data {} and dao is {}",data, dao);
        }
    }

}
