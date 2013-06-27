package org.grouplens.lenskit.eval.config;

import groovy.util.AntBuilder;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * Customized Ant builder that doesn't run tasks.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitAntBuilder extends AntBuilder {
    public LenskitAntBuilder() {
    }

    public LenskitAntBuilder(Project project) {
        super(project);
    }

    public LenskitAntBuilder(Project project, Target owningTarget) {
        super(project, owningTarget);
    }

    public LenskitAntBuilder(Task parentTask) {
        super(parentTask);
    }

    @Override
    protected void nodeCompleted(Object parent, Object node) {
        // Pass a useless (non-target) object as the parent, so superclass logic doesn't run task
        super.nodeCompleted("null", node);
    }
}
