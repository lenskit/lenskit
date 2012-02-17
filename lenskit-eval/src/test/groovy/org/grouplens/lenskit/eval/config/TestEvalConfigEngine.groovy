package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.Evaluation
import org.grouplens.lenskit.eval.traintest.TTPredictEvaluation
import org.junit.Before
import org.junit.Test
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * Test the eval config engine and make sure it can actually load tests.
 * @author Michael Ekstrand
 */
class TestEvalConfigEngine {
    EvalConfigEngine engine;

    @Before
    void createEngine() {
        engine = new EvalConfigEngine()
    }

    private def script(name) {
        return getClass().getResourceAsStream(name)
    }

    @Test
    void testEmptyScript() {
        List<Evaluation> evals = engine.load(script("empty.groovy"))
        assertThat(evals, empty())
    }

    @Test
    void testSingleEmptyEval() {
        List<Evaluation> evals = engine.load(script("emptyTrainTest.groovy"))
        assertThat(evals, hasSize(1))
        def eval = evals.get(0)
        assertThat(eval, instanceOf(TTPredictEvaluation))
        assertThat(eval.getJobGroups(), empty())
    }
}
