package org.grouplens.lenskit.eval.config

import org.apache.tools.ant.Target

/**
 * Delegate to build a target.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TargetDelegate {
    private Target target

    TargetDelegate(Target tgt) {
        target = tgt
    }

    void requires(Object... targets) {
        for (tgt  in targets) {
            if (tgt instanceof Target) {
                target.addDependency(tgt.name)
            } else {
                target.addDependency(tgt)
            }
        }
    }

    void perform(Closure cl) {
        def task = new GroovyActionTask(cl)
        task.setProject(target.project)
        target.addTask(task)
    }
}
