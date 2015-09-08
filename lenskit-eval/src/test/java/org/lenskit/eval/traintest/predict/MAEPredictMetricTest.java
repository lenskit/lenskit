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

public class MAEPredictMetricTest {
    MAEPredictMetric metric;

    @Before
    public void createMetric() {
        metric = new MAEPredictMetric();
    }

    @Test
    public void testColumnLabels(){
        assertThat(metric.getAggregateColumnLabels(),
                   containsInAnyOrder("MAE.ByUser", "MAE.ByRating"));
        assertThat(metric.getColumnLabels(), contains("MAE"));
    }

    @Test
    public void testOneRating() {
        UserHistory<Event> user = History.forUser(42);
        Long2DoubleMap ratings = Long2DoubleMaps.singleton(37, 3.5);
        ResultMap predictions = Results.newResultMap(Results.create(37, 4.0));
        MAEPredictMetric.Context ctx = metric.createContext(null, null, null);

        MetricResult result = metric.measureUser(user, ratings, predictions, ctx);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(MAEPredictMetric.UserResult.class));
        assertThat(result.getValues().get("MAE"),
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
        MAEPredictMetric.Context ctx = metric.createContext(null, null, null);

        MetricResult result = metric.measureUser(user, ratings, predictions, ctx);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(MAEPredictMetric.UserResult.class));
        assertThat(result.getValues().get("MAE"),
                   allOf(instanceOf(Double.class),
                         (Matcher) closeTo(1.0, 1.0e-6)));
    }
}
