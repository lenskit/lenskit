package org.grouplens.lenskit.eval.config;

import groovy.lang.Closure;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * A basic Ant task that executes a Groovy action. These tasks are added by {@link
 * org.grouplens.lenskit.eval.config.TargetDelegate#perform(Closure)}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.2
 */
public class GroovyActionTask extends Task {
    private Closure<?> closure;

    public GroovyActionTask(Closure<?> cl) {
        closure = cl;
    }

    @Override
    public void execute() throws BuildException {
        closure.call();
    }
}
