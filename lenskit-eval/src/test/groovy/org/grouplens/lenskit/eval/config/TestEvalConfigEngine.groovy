package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.Evaluation
import org.grouplens.lenskit.eval.traintest.TTPredictEvaluation
import org.junit.Before
import org.junit.Test
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

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
        return new InputStreamReader(getClass().getResourceAsStream(name), "UTF-8")
    }

    @Test
    void testSingleEmptyEval() {
        List<Evaluation> evals = engine.load(script("emptyTrainTest.groovy"))
        assertThat(evals.size(), equalTo(1))
        def eval = evals.get(0)
        assertThat(eval, instanceOf(TTPredictEvaluation))
        assertTrue(eval.getJobGroups().isEmpty())
    }
}
