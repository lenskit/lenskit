package org.grouplens.lenskit.eval.config

import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task

/**
 * A basic Ant task that executes a Groovy action.
 * These tasks are added by {@link TargetDelegate#perform(Closure)}.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class GroovyActionTask extends Task {
    private Closure closure

    GroovyActionTask(Closure cl) {
        closure = cl
    }

    @Override
    void execute() throws BuildException {
        closure.call()
    }
}
