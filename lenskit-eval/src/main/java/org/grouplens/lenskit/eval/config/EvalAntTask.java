package org.grouplens.lenskit.eval.config;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.grouplens.lenskit.eval.EvalTask;
import org.grouplens.lenskit.eval.TaskExecutionException;

/**
 * Wrap an {@link EvalTask} as an Ant {@link Task}.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class EvalAntTask extends Task {
    private EvalTask<?> evalTask;

    public EvalAntTask() {
        evalTask = null;
    }

    public EvalAntTask(EvalTask<?> task) {
        evalTask = task;
    }

    public EvalTask<?> getEvalTask() {
        return evalTask;
    }

    public void setEvalTask(EvalTask<?> task) {
        evalTask = task;
    }

    @Override
    public void execute() throws BuildException {
        try {
            evalTask.call();
        } catch (TaskExecutionException e) {
            throw new BuildException("error running eval task", e);
        }
    }
}
