package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.EvalTask
import org.grouplens.lenskit.eval.TaskExecutionException

/**
 * A mock task for use in testing.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class MockTask implements EvalTask {
    Closure action

    void setAction(Closure cl) {
        action = cl
    }

    @Override
    Object call() throws TaskExecutionException {
        action.call()
    }
}
