package org.grouplens.lenskit.eval;

/**
 * Builder for phony meta-tasks.
 * @author Michael Ekstrand
 * @since 0.10
 */
public class PhonyEvalTaskBuilder extends AbstractEvalTaskBuilder<PhonyEvalTask> {
    public PhonyEvalTaskBuilder() {
        this("all");
    }

    public PhonyEvalTaskBuilder(String name) {
        super(name);
    }

    @Override
    public PhonyEvalTask build() {
        return new PhonyEvalTask(name, dependencies);
    }
}
