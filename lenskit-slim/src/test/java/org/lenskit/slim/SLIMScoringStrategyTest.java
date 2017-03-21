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

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.number.IsCloseTo.closeTo;
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
    private static Long2DoubleMap weights;
    private Long2ObjectMap<Long2DoubleMap> innerProducts;
    private static final int userNum = 100; // number of total user
    private static final int itemNum = 20; // upper bound of item id (exclusive)


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
            int maxW = rnd.nextInt(maxWeight) + 1;
            weights.put(w, maxW*rnd.nextDouble());
        }
        weights = filterValues(weights, 0.0);
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

        //int userNum = 200; // number of total user
        int maxRatingNum = 10; // each user's max possible rating number (less than maxItemId)
        int maxUserId = 5000; // greater than userNum (userId not necessarily ranging from 0 to userNum)
        double ratingRange = 5.0;

        int size = 0;
        while (size < userNum) {

            long userId = rndGen.nextInt(maxUserId); // produce random user Id
            int userRatingNum = rndGen.nextInt(maxRatingNum) + 1; // produce random total rating number of a user
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

    /**
     * Get a map of item IDs to item-item inner-products including item-label inner-products in the position of maxItemId {@code maxItemId}
     * @param itemVectors Map of item IDs to item ratings
     * @param labels Label vector
     * @param maxItemId A key of item-item inner-product vector which mapping to the value of item-label inner-product
     * @return Map of item IDs to item-item inner-products
     */
    static Long2ObjectMap<Long2DoubleMap> createInnerProducts(Long2ObjectMap<Long2DoubleMap> itemVectors, Long2DoubleMap labels, long maxItemId) {
        Long2ObjectMap<Long2DoubleMap> innerProducts = new Long2ObjectOpenHashMap<>();
        LongOpenHashBigSet itemIdSet = new LongOpenHashBigSet(itemVectors.keySet());
        Iterator<Map.Entry<Long, Long2DoubleMap>> iter = itemVectors.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Long, Long2DoubleMap> entry = iter.next();
            long temIId = entry.getKey();
            Long2DoubleMap itemIRatings = entry.getValue();
            itemIdSet.remove(temIId);

            for (long itemJId : itemIdSet) {
                Long2DoubleMap itemJRatings = itemVectors.get(itemJId);
                double innerProduct = Vectors.dotProduct(itemIRatings, itemJRatings);
                double innerProdItemILabel = Vectors.dotProduct(itemIRatings, labels);
                double innerProdItemJLabel = Vectors.dotProduct(itemJRatings, labels);

                Long2DoubleMap dotJIs = innerProducts.get(itemJId);
                Long2DoubleMap dotIJs = innerProducts.get(temIId);
                if (dotJIs == null) dotJIs = new Long2DoubleOpenHashMap();
                if (dotIJs == null) dotIJs = new Long2DoubleOpenHashMap();
                dotJIs.put(temIId, innerProduct);
                dotJIs.put(maxItemId, innerProdItemJLabel);
                dotIJs.put(itemJId, innerProduct);
                dotIJs.put(maxItemId, innerProdItemILabel);
                innerProducts.put(itemJId, dotJIs);
                innerProducts.put(temIId, dotIJs);
            }
        }
        return innerProducts;
    }


    @Before
    public void buildModel() {
        int maxWeight = 5;
        weights = createWeights(itemNum, maxWeight);
        Long2ObjectMap<Long2DoubleMap> dataWithLabels = new Long2ObjectOpenHashMap<>(createDataModel(itemNum, weights));
        LongOpenHashBigSet userIdSet = new LongOpenHashBigSet(dataWithLabels.keySet());
        long maxUserId = Collections.max(userIdSet);
        y = new Long2DoubleOpenHashMap(dataWithLabels.get(maxUserId));
        dataWithLabels.remove(maxUserId);
        data = transposeMap(dataWithLabels);
        innerProducts = createInnerProducts(data, y, itemNum);
    }

    @Test
    public void testTrainedWeights() {

//        StopWatch timer = new StopWatch();
        SLIMUpdateParameters parameters = new SLIMUpdateParameters(3, 0.2, false, new IterationCountStoppingCondition(50));

        // Naive update
//        timer.start();
        NaiveUpdate model = new NaiveUpdate(parameters);
        Long2DoubleMap predictedW = model.fit(y, data);
//        timer.stop();

        // Covariance update
//        timer.reset();
//        timer.start();
        CovarianceUpdate modelCov = new CovarianceUpdate(parameters);
        Long2DoubleMap predictedWCov = modelCov.fit(y, data);
//        timer.stop();

        // Covariance update with pre-compute inner-products
//        timer.reset();
//        timer.start();
        Long2DoubleMap predictedWCovPreComPuted = modelCov.fit(y, data, innerProducts, itemNum);
//        timer.stop();
        Iterator<Map.Entry<Long,Double>> iter = weights.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Long,Double> entry = iter.next();
            long id = entry.getKey();
            double value = entry.getValue();
            assertThat(value, closeTo(predictedW.get(id), 1));
            assertThat(value, closeTo(predictedWCov.get(id), 1));
            assertThat(value, closeTo(predictedWCovPreComPuted.get(id), 1));
        }

    }

    @Test
    public void testLossFunctionAfterTraining() {
        final SLIMUpdateParameters parameters = new SLIMUpdateParameters(3, 0.2, false, new IterationCountStoppingCondition(50));

        //Naive update
        NaiveUpdate model = new NaiveUpdate(parameters);
        Long2DoubleMap predictedW = model.fit(y, data);
        Long2DoubleMap resBefore = model.computeResiduals(y, data, weights);
        Long2DoubleMap resAfter = model.computeResiduals(y, data, predictedW);
        double lossFunBefore = model.computeLossFunction(resBefore, weights);
        double lossFunAfter = model.computeLossFunction(resAfter, predictedW);

        //Covariance update
        CovarianceUpdate modelCov = new CovarianceUpdate(parameters);
        Long2DoubleMap predictedWCov = modelCov.fit(y, data);
        Long2DoubleMap resCovBefore = modelCov.computeResiduals(y, data, weights);
        Long2DoubleMap resCovAfter = modelCov.computeResiduals(y, data, predictedWCov);
        double lossFunCovBefore = modelCov.computeLossFunction(resCovBefore, weights);
        double lossFunCovAfter = modelCov.computeLossFunction(resCovAfter, predictedWCov);

        //Covariance update with pre-computed inner-products
        Long2DoubleMap predictedWCovPreComputed = modelCov.fit(y, data, innerProducts, itemNum);
        Long2DoubleMap resCovPreComputedAfter = modelCov.computeResiduals(y, data, predictedWCovPreComputed);
        double lossFunCovPreComputedAfter = modelCov.computeLossFunction(resCovPreComputedAfter, predictedWCovPreComputed);

        assertThat(lossFunBefore, equalTo(lossFunCovBefore));
        assertThat(lossFunAfter, lessThan(lossFunBefore));
        assertThat(lossFunCovAfter, lessThan(lossFunCovBefore));
        assertThat(lossFunCovPreComputedAfter, lessThan(lossFunCovBefore));

        assertThat(lossFunAfter, closeTo(lossFunCovAfter, 1.0e-5));
        assertThat(lossFunCovAfter, closeTo(lossFunCovPreComputedAfter, 1.0e-5));

    }

    @Test
    public void testComputeResiduals() {
        Long2DoubleMap col1 = new Long2DoubleOpenHashMap();
        col1.put(1L, 2.0);
        col1.put(3L, 5.0);
        Long2DoubleMap col5 = new Long2DoubleOpenHashMap();
        col5.put(1L, 3.0);
        col5.put(2L, 3.0);
        Long2DoubleMap col3 = new Long2DoubleOpenHashMap();
        col3.put(2L, 4.0);
        col3.put(4L, 5.0);
        Long2ObjectMap<Long2DoubleMap> data = new Long2ObjectOpenHashMap<>();
        data.put(1L, col1);
        data.put(5L, col5);
        data.put(3L, col3);

        Long2DoubleMap labels = new Long2DoubleOpenHashMap();
        labels.put(1L, 5.0);
        labels.put(2L, 4.0);
        labels.put(3L, 2.0);
        labels.put(4L, 6.0);

        Long2DoubleMap weight1 = new Long2DoubleOpenHashMap();
        Long2DoubleMap weight2 = new Long2DoubleOpenHashMap();
        weight2.put(2L, 2.0);
        weight2.put(4L, 2.0);
        Long2DoubleMap weight3 = new Long2DoubleOpenHashMap();
        weight3.put(1L, 2.0);
        weight3.put(2L, 3.0);
        weight3.put(3L, 4.0);
        weight3.put(5L, 5.0);

        final SLIMUpdateParameters parameters = new SLIMUpdateParameters(3, 0.2, false, new IterationCountStoppingCondition(50));

        //Naive update
        NaiveUpdate model = new NaiveUpdate(parameters);
        Long2DoubleMap resNaiveWeight1 = model.computeResiduals(labels, data, weight1);
        Long2DoubleMap resNaiveWeight2 = model.computeResiduals(labels, data, weight2);
        Long2DoubleMap resNaiveWeight3 = model.computeResiduals(labels, data, weight3);

        //Covariance update
        CovarianceUpdate modelCov = new CovarianceUpdate(parameters);
        Long2DoubleMap resCovWeight1 = modelCov.computeResiduals(labels, data, weight1);
        Long2DoubleMap resCovWeight2 = modelCov.computeResiduals(labels, data, weight2);
        Long2DoubleMap resCovWeight3 = modelCov.computeResiduals(labels, data, weight3);

        Long2DoubleMap resExpected = new Long2DoubleOpenHashMap();
        resExpected.put(1L, -14.0);
        resExpected.put(2L, -27.0);
        resExpected.put(3L, -8.0);
        resExpected.put(4L, -14.0);

        assertThat(resNaiveWeight1, is(labels));
        assertThat(resNaiveWeight2, is(labels));
        assertThat(resNaiveWeight3, is(resExpected));

        assertThat(resCovWeight1, is(resNaiveWeight1));
        assertThat(resCovWeight2, is(resNaiveWeight2));
        assertThat(resCovWeight3, is(resNaiveWeight3));

    }

}