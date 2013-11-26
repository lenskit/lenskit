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
package org.grouplens.lenskit.util.parallel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A group of tasks to be run.  Tasks can be added until the group is executed.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
public class TaskGroup {
    private static final Logger logger = LoggerFactory.getLogger(TaskGroup.class);
    private static final AtomicInteger unnamedCount = new AtomicInteger(0);
    private final String name;
    private final List<FutureTask<?>> futures = Lists.newArrayList();
    private volatile boolean started = false;

    /**
     * Create a new task group with a default name.
     */
    public TaskGroup() {
        this("unnamed" + unnamedCount.incrementAndGet());
    }

    /**
     * Create a new named task group.
     * @param name The name of the task group.
     */
    public TaskGroup(String name) {
        this.name = name;
    }

    /**
     * Get the name of this task group.
     * @return The task group's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of tasks in this task group.
     * @return The number of tasks in the task group.
     */
    public synchronized int getTaskCount() {
        return futures.size();
    }

    /**
     * Add a task to the group.
     *
     * @param task The task to add.
     * @throws IllegalStateException if the group is already executing.
     */
    public synchronized void addTask(Runnable task) {
        Preconditions.checkState(!started, "task group already started");
        futures.add(new GroupMemberFuture<Void>(task, null));
    }

    /**
     * Add a task to the group.
     *
     * @param task The task to add.
     * @return A future continaing the result of this task.
     * @throws IllegalStateException if the group is already executing.
     */
    public synchronized <V> Future<V> addTask(Callable<V> task) {
        Preconditions.checkState(!started, "task group already started");
        FutureTask<V> result = new GroupMemberFuture<V>(task);
        futures.add(result);
        return result;
    }

    /**
     * Add a list of tasks to the group.
     *
     * @param tasks The tasks to add.
     * @throws IllegalStateException if the group is already executing.
     */
    @SuppressWarnings("unchecked")
    public synchronized void addAll(Iterable<? extends Callable<?>> tasks) {
        Preconditions.checkState(!started, "task group already started");
        for (Callable<?> task: tasks) {
            addTask(task);
        }
    }

    /**
     * Execute the tasks in this group.
     *
     * <p>If a task fails (throws an exception), the remaining tasks are cancelled and the exception
     * is reported as an execution exception.  If multiple tasks throw an exception (e.g. they are
     * started concurrently, so they don't get cancelled, and another fails), the exception thrown
     * by the one added to the group first is the one that is thrown.  Others are logged.</p>
     *
     * @param exec The executor to use.
     * @throws ExecutionException if a task fails.
     */
    public void execute(ExecutorService exec) throws ExecutionException {
        synchronized (this) {
            Preconditions.checkState(!started, "task group already started");
            started = true;
        }
        // now we can run, any other thread will detect that started is in progress

        logger.info("running {} tasks in group {}", futures, name);
        // queue up all the tasks
        for (FutureTask<?> task: futures) {
            exec.submit(task);
        }

        // wait for all the tasks
        Exception error = null;
        int cancelled = 0;
        for (FutureTask<?> task: futures) {
            boolean retrieved = false;
            while (!retrieved) {
                try {
                    task.get();
                    retrieved = true;
                } catch (InterruptedException e) {
                    /* do nothing, try again */
                } catch (CancellationException ex) {
                    logger.debug("{} cancelled", task);
                    cancelled += 1;
                    retrieved = true;
                } catch (Exception ex) {
                    if (error == null) {
                        logger.error("error in " + task, ex);
                        error = ex;
                    } else {
                        logger.error("ignoring error in " + task + " (previous task already errored)",
                                     ex);
                    }
                    retrieved = true;
                }
            }
        }

        if (error == null && cancelled == 0) {
            logger.warn("{} tasks cancelled but no errors", cancelled);
        }

        if (error instanceof ExecutionException) {
            throw (ExecutionException) error;
        } else if (error != null) {
            throw new ExecutionException("unknown error started task", error);
        }
    }

    private class GroupMemberFuture<V> extends FutureTask<V> {
        private String descr;
        private GroupMemberFuture(Callable<V> callable) {
            super(callable);
            descr = callable.toString();
        }

        private GroupMemberFuture(Runnable runnable, V result) {
            super(runnable, result);
            descr = runnable.toString();
        }

        @Override
        protected void setException(Throwable t) {
            super.setException(t);
            for (Future<?> task: futures) {
                // TODO Allow interrupts
                task.cancel(false);
            }
        }

        @Override
        public String toString() {
            return "task " + descr;
        }
    }
}
