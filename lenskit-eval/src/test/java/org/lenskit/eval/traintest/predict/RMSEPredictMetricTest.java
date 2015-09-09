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
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ResultMap;
import org.lenskit.data.events.Event;
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
        UserHistory<Event> user = History.forUser(42);
        Long2DoubleMap ratings = Long2DoubleMaps.singleton(37, 3.5);
        ResultMap predictions = Results.newResultMap(Results.create(37, 4.0));
        RMSEPredictMetric.Context ctx = metric.createContext(null, null, null);

        MetricResult result = metric.measureUser(user, ratings, predictions, ctx);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(RMSEPredictMetric.UserResult.class));
        assertThat(result.getValues().get("RMSE"),
                   allOf(instanceOf(Double.class),
                         (Matcher) closeTo(0.5, 1.0e-6)));
    }

    @Test
    public void testTwoRatings() {
        UserHistory<Event> user = History.forUser(42);
        Long2DoubleMap ratings = new Long2DoubleOpenHashMap();
        ratings.put(37, 3.5);
        ratings.put(12, 2.0);
        ResultMap predictions = Results.newResultMap(Results.create(37, 4.0),
                                                     Results.create(12, 3.5));
        RMSEPredictMetric.Context ctx = metric.createContext(null, null, null);

        MetricResult result = metric.measureUser(user, ratings, predictions, ctx);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(RMSEPredictMetric.UserResult.class));
        assertThat(result.getValues().get("RMSE"),
                   allOf(instanceOf(Double.class),
                         (Matcher) closeTo(Math.sqrt((0.25 + 2.25) / 2), 1.0e-6)));
    }
}
