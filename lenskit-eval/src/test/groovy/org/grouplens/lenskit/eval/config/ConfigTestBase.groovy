package org.grouplens.lenskit.eval.config

import org.junit.Before

/**
 * Base/helper class for testing configuration code snippets. Provides an {@link #eval(Closure)}
 * method which runs a code snippet as if it were a config script and returns the result.
 * @author Michael Ekstrand
 */
abstract class ConfigTestBase {
    protected EvalConfigEngine engine

    @Before
    public void createEngine() {
        engine = new EvalConfigEngine()
    }

    /**
     * Evalate a closure as if it were a config snippet.
     * @param cl The code to run.
     * @return The return value of evaluating {@code cl}.
     */
    protected def eval(Closure cl) {
        def script = new ClosureScript(engine, cl)
        return script.run()
    }
}
