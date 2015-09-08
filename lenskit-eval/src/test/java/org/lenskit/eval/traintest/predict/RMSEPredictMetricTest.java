package org.lenskit.eval.traintest.predict;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
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
}