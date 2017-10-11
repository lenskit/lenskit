/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.parallel;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * A group of tasks to be executed in a fork-join tree.
 */
public class TaskGroup extends RecursiveAction {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TaskGroup.class);

    private boolean parallel;
    private boolean continueAfterError = false;
    private Deque<ForkJoinTask<?>> tasks;

    /**
     * Create a new task group.
     * @param par `true` to execute the subtasks in parallel.
     */
    public TaskGroup(boolean par) {
        parallel = par;
        tasks = new LinkedList<>();
    }

    /**
     * Query whether the subtasks will be run in parallel.
     * @return `true` if the subtasks are run in parallel.
     */
    public boolean isParallel() {
        return parallel;
    }

    public void setContinueAterError(boolean c) {
        continueAfterError = c;
    }

    public boolean getContinueAfterError() {
        return continueAfterError;
    }

    /**
     * Add a task to be executed.
     * @param task The task to execute.
     */
    public void addTask(ForkJoinTask<?> task) {
        Preconditions.checkState(!isDone(), "task already completed");
        tasks.add(task);
    }

    @Override
    public void compute() {
        if (parallel) {
            logger.debug("running {} tasks in parallel", tasks.size());
            if (continueAfterError) {
                runAll();
            } else {
                invokeAll(tasks);
            }
        } else {
            logger.debug("running {} tasks in sequence", tasks.size());
            Throwable failure = null;
            while (!tasks.isEmpty()) {
                ForkJoinTask<?> task = tasks.removeFirst();
                try {
                    task.invoke();
                } catch (Throwable th) {
                    if (continueAfterError) {
                        if (failure == null) {
                            failure = th;
                        } else {
                            failure.addSuppressed(th);
                        }
                    } else {
                        Throwables.throwIfUnchecked(th);
                        throw new UncheckedExecutionException(th);
                    }
                }
            }
            if (failure != null) {
                Throwables.throwIfUnchecked(failure);
                throw new UncheckedExecutionException(failure);
            }
        }
    }

    private void runAll() {
        for (ForkJoinTask<?> task: tasks) {
            task.fork();
        }
        Throwable failure = null;
        for (ForkJoinTask<?> task: tasks) {
            try {
                task.join();
            } catch (Throwable th) {
                logger.error("job " + task + " failed with exception", th);
                if (failure == null) {
                    failure = th;
                } else {
                    failure.addSuppressed(th);
                }
            }
        }
        if (failure != null) {
            throw new UncheckedExecutionException("Error running a subjob", failure);
        }
    }
}
