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

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ResultMap;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.results.Results;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class RMSEPredictMetricTest {
    RMSEPredictMetric metric;

    @Before
    public void createMetric() {
        metric = new RMSEPredictMetric();
    }

    @Test
    public void testColumnLabels(){
        assertThat(metric.getAggregateColumnLabels(),
                   containsInAnyOrder("RMSE.ByUser", "RMSE.ByRating"));
        assertThat(metric.getColumnLabels(), contains("RMSE"));
    }

    @Test
    public void testOneRating() {
        TestUser user = TestUser.newBuilder()
                                .setUserId(42)
                                .addTestRating(37, 3.5)
                                .build();
        ResultMap predictions = Results.newResultMap(Results.create(37, 4.0));
        RMSEPredictMetric.Context ctx = metric.createContext(null, null, null);

        MetricResult result = metric.measureUser(user, predictions, ctx);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(RMSEPredictMetric.UserResult.class));
        assertThat(result.getValues().get("RMSE"),
                   allOf(instanceOf(Double.class),
                         (Matcher) closeTo(0.5, 1.0e-6)));
    }

    @Test
    public void testTwoRatings() {
        TestUser user = TestUser.newBuilder()
                                .setUserId(42)
                                .addTestRating(37, 3.5)
                                .addTestRating(12, 2.0)
                                .build();
        ResultMap predictions = Results.newResultMap(Results.create(37, 4.0),
                                                     Results.create(12, 3.5));
        RMSEPredictMetric.Context ctx = metric.createContext(null, null, null);

        MetricResult result = metric.measureUser(user, predictions, ctx);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(RMSEPredictMetric.UserResult.class));
        assertThat(result.getValues().get("RMSE"),
                   allOf(instanceOf(Double.class),
                         (Matcher) closeTo(Math.sqrt((0.25 + 2.25) / 2), 1.0e-6)));
    }
}
