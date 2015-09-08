package org.lenskit.eval.traintest;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.specs.eval.PredictEvalTaskSpec;
import org.lenskit.specs.eval.TrainTestExperimentSpec;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TrainTestExperimentSpecTest {
    TrainTestExperimentSpec spec;

    @Before
    public void newSpec() {
        spec = new TrainTestExperimentSpec();
    }

    @Test
    public void testNonEmpty() {
        TrainTestExperiment exp = TrainTestExperiment.fromSpec(spec);
        assertThat(exp, notNullValue());
        assertThat(exp.getOutputFile(), nullValue());
    }

    @Test
    public void testOutputFiles() {
        spec.setOutputFile(Paths.get("results.csv"));
        spec.setUserOutputFile(Paths.get("users.csv"));
        PredictEvalTaskSpec ets = new PredictEvalTaskSpec();
        ets.setOutputFile(Paths.get("predictions.csv"));
        spec.addTask(ets);
        assertThat(spec.getOutputFiles(),
                   containsInAnyOrder(Paths.get("results.csv"),
                                      Paths.get("users.csv"),
                                      Paths.get("predictions.csv")));
        TrainTestExperiment exp = TrainTestExperiment.fromSpec(spec);
        assertThat(exp.getOutputFile(),
                   equalTo(Paths.get("results.csv")));
        assertThat(exp.getUserOutputFile(),
                   equalTo(Paths.get("users.csv")));
        assertThat(exp.getPredictionTask().getOutputFile(),
                   equalTo(Paths.get("predictions.csv")));
    }
}
