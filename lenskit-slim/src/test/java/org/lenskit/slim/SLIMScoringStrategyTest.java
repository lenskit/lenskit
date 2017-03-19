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
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.iterative.IterationCountStoppingCondition;
import org.junit.Before;
import org.junit.Test;

import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.lenskit.slim.LinearRegressionHelper.*;

/**
 * SLIMScoringStrategy test.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SLIMScoringStrategyTest {
    private Long2DoubleMap y;
    private Long2ObjectMap<Long2DoubleMap> data;
    private Long2DoubleMap weights;
    final static Logger logger = LoggerFactory.getLogger(SLIMScoringStrategyTest.class);

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
        return LongUtils.frozenMap(weights);
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

        int userNum = 200; // number of total user
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

    @Before
    public void buildModel() {
        int itemNum = 100;
        int maxWeight = 5;
        weights = createWeights(itemNum, maxWeight);
        Long2ObjectMap<Long2DoubleMap> dataWithLabels = new Long2ObjectOpenHashMap<>(createDataModel(itemNum, weights));
        LongOpenHashBigSet userIdSet = new LongOpenHashBigSet(dataWithLabels.keySet());
        long maxUserId = Collections.max(userIdSet);
        y = new Long2DoubleOpenHashMap(dataWithLabels.get(maxUserId));
        dataWithLabels.remove(maxUserId);
        data = transposeMap(dataWithLabels);
        LongOpenHashBigSet itemKeySet = new LongOpenHashBigSet(data.keySet());
        //logger.info("item matrix size {} \n max item id is {} \n min item id is {} ",itemKeySet.size64(), Collections.max(itemKeySet), Collections.min(itemKeySet));
    }

    @Test
    public void testNaiveUpdate() {

        StopWatch timer = new StopWatch();
        SLIMUpdateParameters parameters = new SLIMUpdateParameters(3, 0.2, false, new IterationCountStoppingCondition(10));

        // Naive update
        timer.start();
        final long startTimeNaive = System.nanoTime();
        NaiveUpdate model = new NaiveUpdate(parameters);
        Long2DoubleMap predictedW = model.fit(y, data);
        //Long2DoubleMap predictions = model.predict(data, predictedW);
        //Long2DoubleMap resBefore = model.computeResiduals(y, data, weights);
        //Long2DoubleMap resAfter = model.computeResiduals(y, data, predictedW);
        //double lossFunBefore = model.computeLossFunction(resBefore, weights);
        //double lossFunAfter = model.computeLossFunction(resAfter, predictedW);
        //System.out.println(lossFunBefore);
        //System.out.println(lossFunAfter);
//        System.out.println(weights);
//        System.out.println(predictedW);
        final long endTimeNaive = System.nanoTime();
        timer.stop();

        // Covariance update
        timer.reset();
        timer.start();
        final long startTimeCov = System.currentTimeMillis();
        CovarianceUpdate modelCov = new CovarianceUpdate(parameters);
        Long2DoubleMap predictedWCov = modelCov.fit(y, data);
        final long endTimeCov = System.currentTimeMillis();
        timer.stop();

        Long2DoubleMap originalW = filterValues(weights, 0.0);
        originalW = LongUtils.frozenMap(originalW);
        Iterator<Map.Entry<Long,Double>> iter = originalW.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Long,Double> entry = iter.next();
            long id = entry.getKey();
            double value = entry.getValue();
            assertThat(value, closeTo(predictedW.get(id), 1));
            assertThat(value, closeTo(predictedWCov.get(id), 1));
        }
        //assertThat(weights, is(weights));
        //assertThat(data, is(data));

//        //Long2DoubleMap predictions = model.predict(data, predictedW);
//        //Long2DoubleMap residuals_Cov = modelCov.computeResiduals(y, data, predictedWCov);
//        //double lossFun_Cov = modelCov.computeLossFunction(residuals_Cov, predictedWCov);
//        //System.out.println(lossFun);

//        System.out.println(predictedWCov);
//        System.out.println(weights);

        //logger.info("running time naive {}", (endTimeNaive - startTimeNaive));
        //logger.info("running time Cov {}", (endTimeCov - startTimeCov));

    }

}