/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.eval.metrics.predict;

import org.grouplens.lenskit.eval.traintest.MockTestUser;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class NDCGTest {
    long[] items = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    double[] ratings1 = {5, 4, 4, 3, 5, 3, 4, 3, 2, 5};
    double[] ratings2 = {5, 5, 4, 4, 4, 3, 2, 2, 3, 4};
    double[] ratings3 = {4, 5, 4, 2, 3, 1, 3, 4, 5, 2};

    double[] predictions1 = {5, 5, 4, 4, 4, 3, 2, 2, 3, 4};
    double[] predictions2 = {4, 4, 5, 2, 3, 2, 3, 4, 4, 3};
    double[] predictions3 = {4, 4, 5, 3, 3, 4, 5, 4, 4, 4};

    SparseVector rv1 = MutableSparseVector.wrap(items, ratings1).freeze();
    SparseVector rv2 = MutableSparseVector.wrap(items, ratings2).freeze();
    SparseVector rv3 = MutableSparseVector.wrap(items, ratings3).freeze();

    SparseVector pv1 = MutableSparseVector.wrap(items, predictions1).freeze();
    SparseVector pv2 = MutableSparseVector.wrap(items, predictions2).freeze();
    SparseVector pv3 = MutableSparseVector.wrap(items, predictions3).freeze();

    TestUser user1, user2, user3;

    @Before
    public void createTestUsers() {
        MockTestUser.Builder b1, b2, b3;

        b1 = MockTestUser.newBuilder().setUserId(1);
        b2 = MockTestUser.newBuilder().setUserId(2);
        b3 = MockTestUser.newBuilder().setUserId(3);
        for (int i = 0; i < 10; i++) {
            b1.addTestRating(items[i], ratings1[i]);
            b2.addTestRating(items[i], ratings2[i]);
            b3.addTestRating(items[i], ratings3[i]);
        }
        user1 = b1.setPredictions(pv1).build();
        user2 = b2.setPredictions(pv2).build();
        user3 = b3.setPredictions(pv3).build();
    }

    @Test
    public void testComputeDCG() {
        // score rating vectors
        assertEquals(22.0418,
                     NDCGPredictMetric.computeDCG(rv1.keysByValue(true), rv1),
                     0.0001);
        assertEquals(21.0954,
                     NDCGPredictMetric.computeDCG(rv2.keysByValue(true), rv2),
                     0.0001);
        assertEquals(20.0742,
                     NDCGPredictMetric.computeDCG(rv3.keysByValue(true), rv3),
                     0.0001);

        // score prediction vectors
        // note that the correctness of these tests depends on keysByValue using
        // a stable sort.
        assertEquals(21.0116,
                     NDCGPredictMetric.computeDCG(pv1.keysByValue(true), rv1),
                     0.0001);
        assertEquals(20.2048,
                     NDCGPredictMetric.computeDCG(pv2.keysByValue(true), rv2),
                     0.0001);
        assertEquals(17.9835,
                     NDCGPredictMetric.computeDCG(pv3.keysByValue(true), rv3),
                     0.0001);
    }

    @Test
    public void testAccumulator() {
        NDCGPredictMetric metric = new NDCGPredictMetric();
        MeanAccumulator acc = metric.createContext(null, null, null);
        assertThat(acc, notNullValue());
        assert acc != null;
        metric.measureUser(user1, acc);
        assertEquals(1, acc.getCount());
        assertEquals(0.9533, acc.getTotal(), 0.0001);
        metric.measureUser(user2, acc);
        assertEquals(2, acc.getCount());
        assertEquals(1.9110, acc.getTotal(), 0.0001);
        metric.measureUser(user3, acc);
        assertEquals(3, acc.getCount());
        assertEquals(2.8069, acc.getTotal(), 0.0001);
    }
}
