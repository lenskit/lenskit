package org.grouplens.lenskit.eval

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.junit.Test

/**
 * @author Michael Ekstrand
 */
class TestPhonyEvalTask extends ConfigTestBase {
    @Test
    public void testPhonyTask() {
        def task = eval {
            phony("foo")
        }
        assertThat(task, instanceOf(PhonyEvalTask))
        assertThat(task.name, equalTo("foo"))
    }

    @Test
    public void testPhonyDeps() {
        def task = eval {
            phony("foo") {
                depends phony("bar")
                depends phony("blam")
            }
        } as EvalTask
        assertThat(task, instanceOf(PhonyEvalTask))
        assertThat(task.name, equalTo("foo"))
        assertThat(task.dependencies.size(), equalTo(2))
        assertThat(task.dependencies.collect({it.name}).contains("bar"), equalTo(true))
    }
}
