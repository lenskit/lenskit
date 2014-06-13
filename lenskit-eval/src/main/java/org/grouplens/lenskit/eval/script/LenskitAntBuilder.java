/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval.script;

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
