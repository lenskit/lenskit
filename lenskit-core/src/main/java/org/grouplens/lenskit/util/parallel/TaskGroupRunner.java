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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Execute a collection of tasks. This uses an executor service to run a group of jobs, and cancels
 * all unfinished jobs when one of them fails.
 *
 * @since 1.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TaskGroupRunner {
    private static Logger logger = LoggerFactory.getLogger(TaskGroupRunner.class);
    private final ListeningExecutorService executor;
    private final Set<Future<?>> activeTasks;
    private final ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<Throwable>();

    private TaskGroupRunner(ListeningExecutorService exec) {
        executor = exec;
        activeTasks = Sets.newHashSet();
    }

    public static TaskGroupRunner create(ExecutorService exc) {
        return new TaskGroupRunner(MoreExecutors.listeningDecorator(exc));
    }

    public synchronized void submit(Runnable task) {
        ListenableFuture<?> result = executor.submit(task);
        activeTasks.add(result);
        Futures.addCallback(result, new Callback(result));
    }

    public void submitAll(Collection<? extends Runnable> tasks) {
        for (Runnable r: tasks) {
            submit(r);
        }
    }

    public void waitForAll() throws ExecutionException {
        synchronized (this) {
            while (!activeTasks.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    /* do nothing, wait again */
                }
            }
        }
        Throwable th = errors.poll();
        if (th != null) {
            throw new ExecutionException(th);
        }
    }

    private synchronized void removeTask(Future<?> result) {
        if (activeTasks.remove(result) && activeTasks.isEmpty()) {
            notifyAll();
        }
    }

    private void cancelRemainingTasks() {
        Set<Future<?>> tasks;
        synchronized (this) {
            tasks = ImmutableSet.copyOf(activeTasks);
        }
        for (Future<?> task: tasks) {
            task.cancel(true);
        }
    }

    private class Callback implements FutureCallback<Object> {
        private final ListenableFuture<?> future;

        public Callback(ListenableFuture<?> f) {
            future = f;
        }

        @Override
        public void onSuccess(Object result) {
            logger.debug("task completed successfully");
            removeTask(future);
        }

        @Override
        public void onFailure(Throwable t) {
            logger.debug("task completed with error", t);
            if (!(t instanceof CancellationException)) {
                errors.add(t);
                cancelRemainingTasks();
            }
            removeTask(future);
        }
    }
}
