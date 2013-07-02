/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Wrap an {@link EvalTask} as an Ant {@link Task}.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class EvalAntTask extends Task {
    private final EvalTask<?> evalTask;
    private final Iterable<? extends Future<?>> dependencies;

    public EvalAntTask(EvalTask<?> task) {
        this(task, Collections.<Future<?>>emptyList());
    }

    /**
     * Construct an Ant wrapper for an eval task.
     * @param task The eval task.
     * @param deps The tasks dependencies. These are not used directly; they are just checked to
     *             make sure they are all satisfied before the task is executed.
     */
    public EvalAntTask(EvalTask<?> task, Iterable<? extends Future<?>> deps) {
        evalTask = task;
        dependencies = deps;
    }

    public EvalTask<?> getEvalTask() {
        return evalTask;
    }

    @Override
    public void execute() throws BuildException {
        for (Future<?> dep: dependencies) {
            if (!dep.isDone()) {
                throw new BuildException("dependency not yet satisfied");
            }
        }
        try {
            evalTask.execute();
        } catch (TaskExecutionException e) {
            throw new BuildException("error running eval task", e);
        }
    }
}
