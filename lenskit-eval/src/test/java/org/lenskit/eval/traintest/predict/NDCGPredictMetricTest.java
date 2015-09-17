package org.lenskit.eval.traintest.predict;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.results.Results;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.nullValue;
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
        MetricResult result = metric.measureUser(History.forUser(42),
                                                 Long2DoubleMaps.EMPTY_MAP, Results.newResultMap(),
                                                 acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   nullValue());
    }

    @Test
    public void testSingleton() {
        MeanAccumulator acc = metric.createContext(null, null, null);
        MetricResult result = metric.measureUser(History.forUser(42),
                                                 Long2DoubleMaps.singleton(10, 3.5),
                                                 Results.newResultMap(Results.create(10, 2.5)),
                                                 acc);
        assertThat((Double) result.getValues().get("Predict.nDCG"),
                   closeTo(1.0, 1.0e-6));
    }

    @Test
    public void testInOrder() {
        MeanAccumulator acc = metric.createContext(null, null, null);
        Long2DoubleMap ratings = new Long2DoubleOpenHashMap();
        ratings.put(1, 3.5);
        ratings.put(2, 3.0);
        ratings.put(3, 2.5);
        MetricResult result = metric.measureUser(History.forUser(42),
                                                 ratings,
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
        MetricResult result = metric.measureUser(History.forUser(42),
                                                 ratings,
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