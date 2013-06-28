package org.grouplens.lenskit.eval.config;

import groovy.lang.Closure;
import groovy.util.AntBuilder;
import org.apache.tools.ant.Target;

/**
 * Delegate to build a target.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.2
 */
public class TargetDelegate {
    private final AntBuilder ant;
    private Target target;

    public TargetDelegate(Target tgt) {
        target = tgt;
        ant = new LenskitAntBuilder(tgt.getProject(), tgt);
    }

    public void requires(Object... targets) {
        for (Object tgt : targets) {
            if (tgt instanceof Target) {
                target.addDependency(((Target) tgt).getName());
            } else {
                target.addDependency(tgt.toString());
            }

        }

    }

    public void perform(Closure<?> cl) {
        GroovyActionTask task = new GroovyActionTask(cl);
        task.setProject(target.getProject());
        target.addTask(task);
    }

    public final AntBuilder getAnt() {
        return ant;
    }
}
