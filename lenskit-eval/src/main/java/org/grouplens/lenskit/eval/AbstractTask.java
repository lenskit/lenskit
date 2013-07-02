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


import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractFuture;
import org.grouplens.lenskit.eval.config.EvalConfig;
import org.grouplens.lenskit.eval.config.EvalProject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class to simplify writing {@link EvalTask}s.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractTask<T> extends AbstractFuture<T> implements EvalTask<T> {
    @Nullable private String name;
    private EvalProject project;

    /**
     * Initialize a command with no name.
     */
    protected AbstractTask() {
        this(null);
    }

    /**
     * Initialize a command.
     * @param name The command's name.
     */
    protected AbstractTask(@Nullable String name) {
        this.name = name;
    }

    /**
     * Set the project this task is a member of.  The evaluation framework automatically
     * calls this method, it is not necessary to call it manually.
     *
     * @param ep The eval project.
     * @return The command (for chaining).
     */
    public AbstractTask<T> setProject(@Nonnull EvalProject ep) {
        Preconditions.checkNotNull(ep, "project cannot be null");
        project = ep;
        return this;
    }

    @Nonnull
    public EvalProject getProject() {
        if (project == null) {
            throw new IllegalStateException("no project configured");
        }
        return project;
    }

    /**
     * Get the command's configuration.
     *
     * @deprecated Use {@link org.grouplens.lenskit.eval.config.EvalProject#getConfig()} directly.
     * @return The command's configuration object.
     */
    @Nonnull
    @Deprecated
    public EvalConfig getEvalConfig() {
        return getProject().getConfig();
    }

    /**
     * Set this command's name.
     *
     * @param name The new name.
     * @return The command (for chaining).
     */
    public AbstractTask<T> setName(@Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the task's name.
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Execute the task.  This calls {@link #perform()}, exposing the return value (or execption)
     * as the task's value.
     *
     * @throws TaskExecutionException
     */
    @Override
    public void execute() throws TaskExecutionException {
        T result;
        try {
            result = perform();
        } catch (Exception ex) {
            setException(ex);
            Throwables.propagateIfPossible(ex, TaskExecutionException.class);
            throw new RuntimeException("unexpected exception", ex);
        }
        set(result);
    }

    protected abstract T perform() throws TaskExecutionException;
}
