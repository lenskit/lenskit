package org.grouplens.lenskit.eval;

import java.util.Set;

/**
 * Phony task to depend on other tasks.
 * @author Michael Ekstrand
 * @since 0.10
 */
public class PhonyEvalTask extends AbstractEvalTask {

    protected PhonyEvalTask(String name, Set<EvalTask> dependencies) {
        super(name, dependencies);
    }

    @Override
    public void execute(EvalOptions options) throws EvalTaskFailedException {
        /* no-op */
    }
}
