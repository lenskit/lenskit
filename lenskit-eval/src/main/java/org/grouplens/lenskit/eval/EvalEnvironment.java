/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An evaluation environment - a set of tasks to run.
 * @author Michael Ekstrand
 */
public class EvalEnvironment {
    private @Nonnull Map<String,EvalTask> taskMap;
    private @Nullable Object scriptResult;
    private @Nonnull List<EvalTask> taskList;

    /**
     * Construct a new eval environment.
     * @param tasks The list of tasks.
     * @param obj The result of evaluating the script.
     * @throws EvaluatorConfigurationException if the task list is invalid
     */
    public EvalEnvironment(@Nonnull List<EvalTask> tasks,
                           @Nullable Object obj)
            throws EvaluatorConfigurationException {
        taskList = tasks;
        taskMap = new HashMap<String, EvalTask>();
        for (EvalTask t: tasks) {
            Preconditions.checkNotNull(t, "task must not be null");
            String name = t.getName();
            if (taskMap.containsKey(name)) {
                throw new EvaluatorConfigurationException("multiple definitions of task " + name);
            } else {
                taskMap.put(name, t);
            }
        }

        scriptResult = obj;
    }

    public List<EvalTask> getTasks() {
        return taskList;
    }

    /**
     * Get a task by name.
     * @param name The name of the task to retrieve.
     * @return The task, or {@code null} if no such task is defined.
     */
    public @Nullable EvalTask getTask(String name) {
        return taskMap.get(name);
    }

    /**
     * Get the result of evaluating the eval script. This is the object returned
     * by the script when it is evaluated. It is also the default task, if it is
     * an {@link EvalTask}.
     * @return The return value of the eval script.
     */
    public @Nullable Object getScriptResult() {
        return scriptResult;
    }

    /**
     * Get the default task.
     * @return The default task, or {@code null} if there is no default task.
     */
    public @Nullable EvalTask getDefaultTask() {
        if (scriptResult instanceof EvalTask) {
            return (EvalTask) scriptResult;
        } else {
            return null;
        }
    }
}
