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
package org.lenskit.eval.traintest.predict;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.results.Results;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class NDCGPredictMetricTest {
    NDCGPredictMetric metric;

    @Before
    public void createMetric() {
        metric = new NDCGPredictMetric();
    }

    @Test
    public void testEmpty() {
        MeanAccumulator acc = metric.createContext(null, null, null);
        MetricResult result = metric.measureUser(TestUser.newBuilder().setUserId(42).build(),
                                                 Results.newResultMap(),
                                                 acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   nullValue());
    }

    @Test
    public void testSingleton() {
        MeanAccumulator acc = metric.createContext(null, null, null);
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
        MeanAccumulator acc = metric.createContext(null, null, null);
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
        MeanAccumulator acc = metric.createContext(null, null, null);
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
