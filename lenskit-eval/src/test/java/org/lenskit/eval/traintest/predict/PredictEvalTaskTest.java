package org.lenskit.eval.traintest.predict;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.specs.eval.PredictEvalTaskSpec;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PredictEvalTaskTest {
    PredictEvalTaskSpec spec;

    @Before
    public void createSpec() {
        spec = new PredictEvalTaskSpec();
    }

    @Test
    public void testCreateFromEmptySpec() {
        PredictEvalTask task = PredictEvalTask.fromSpec(spec);
        assertThat(task.getOutputFile(), nullValue());
    }

    @Test
    public void testConfigureOutputFile() {
        spec.setOutputFile(Paths.get("foo.csv"));
        PredictEvalTask task = PredictEvalTask.fromSpec(spec);
        assertThat(task.getOutputFile(),
                   equalTo(Paths.get("foo.csv")));
    }

    @Test
    public void testConfigureMetrics() {
        spec.addMetric("rmse");
        spec.addMetric("coverage");
        PredictEvalTask task = PredictEvalTask.fromSpec(spec);
        assertThat(task.getPredictMetrics(),
                   contains(instanceOf(RMSEPredictMetric.class),
                            instanceOf(CoveragePredictMetric.class)));
    }
}
