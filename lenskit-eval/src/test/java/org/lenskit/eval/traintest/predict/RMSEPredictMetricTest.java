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
