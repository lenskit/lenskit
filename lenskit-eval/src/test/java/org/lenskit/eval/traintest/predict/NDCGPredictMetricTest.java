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
package org.lenskit.eval.traintest.predict;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.results.Results;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class NDCGPredictMetricTest {
    NDCGPredictMetric metric;

    @Before
    public void createMetric() {
        metric = new NDCGPredictMetric();
    }

    @Test
    public void testEmpty() {
        Mean acc = metric.createContext(null, null, null);
        MetricResult result = metric.measureUser(TestUser.newBuilder().setUserId(42).build(),
                                                 Results.newResultMap(),
                                                 acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   nullValue());
    }

    @Test
    public void testSingleton() {
        Mean acc = metric.createContext(null, null, null);
        MetricResult result = metric.measureUser(TestUser.newBuilder()
                                                         .setUserId(42)
                                                         .addTestRating(10, 3.5)
                                                         .build(),
                                                 Results.newResultMap(Results.create(10, 2.5)),
                                                 acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   closeTo(1.0, 1.0e-6));
    }

    @Test
    public void testInOrder() {
        Mean acc = metric.createContext(null, null, null);
        MetricResult result = metric.measureUser(TestUser.newBuilder()
                                                         .setUserId(42)
                                                         .addTestRating(1, 3.5)
                                                         .addTestRating(2, 3.0)
                                                         .addTestRating(3, 2.5)
                                                         .build(),
                                                 Results.newResultMap(Results.create(1, 1),
                                                                      Results.create(2, 0.8),
                                                                      Results.create(3, 0.5)),
                                                 acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   closeTo(1.0, 1.0e-6));
        assertThat((Double) result.getValues().get("Predict.nDCG.Raw"),
                   closeTo(8.077324384, 1.0e-6));
        result = metric.getAggregateMeasurements(acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   closeTo(1.0, 1.0e-5));
    }

    @Test
    public void testNotInOrder() {
        Mean acc = metric.createContext(null, null, null);
        Long2DoubleMap ratings = new Long2DoubleOpenHashMap();
        ratings.put(1, 3.5);
        ratings.put(2, 3.0);
        ratings.put(3, 2.5);
        MetricResult result = metric.measureUser(TestUser.newBuilder()
                                                         .setUserId(42)
                                                         .addTestRating(1, 3.5)
                                                         .addTestRating(2, 3.0)
                                                         .addTestRating(3, 2.5)
                                                         .build(),
                                                 Results.newResultMap(Results.create(1, 1),
                                                                      Results.create(2, 0.5),
                                                                      Results.create(3, 0.8)),
                                                 acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   closeTo(0.977154, 1.0e-5));
        assertThat((Double) result.getValues().get("Predict.nDCG.Raw"),
                   closeTo(7.892789261, 1.0e-6));
        result = metric.getAggregateMeasurements(acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   closeTo(0.977154, 1.0e-5));
    }
}
