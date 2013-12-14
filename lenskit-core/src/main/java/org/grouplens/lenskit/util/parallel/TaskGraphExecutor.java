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

import org.grouplens.grapht.graph.DAGNode;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Run tasks with (pre-computed) dependencies.  Task graphs are represented by Grapht DAG nodes.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class TaskGraphExecutor {
    /**
     * Create a task graph runner that uses the specified number of threads.
     * @param nthreads The number of threads (at least 1).
     * @param name The name of the task executor (used to name threads)
     * @return A new task graph runner.
     */
    public static TaskGraphExecutor create(int nthreads, String name) {
        return new ParallelTaskGraphExecutor(nthreads, name);
    }

    /**
     * Create a task graph runner that uses the specified number of threads.
     * @param nthreads The number of threads (at least 1).
     * @return A new task graph runner.
     */
    public static TaskGraphExecutor create(int nthreads) {
        return create(nthreads, "graph-executor");
    }

    /**
     * Create a task graph runner that uses {@link Runtime#availableProcessors()} threads.
     *
     * @return A new task graph runner.
     */
    public static TaskGraphExecutor create() {
        return create(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Return a single-threaded task graph runner.
     * @return The runner.
     */
    public static TaskGraphExecutor singleThreaded() {
        return new SequentialTaskGraphExecutor();
    }

    /**
     * Execute a task graph.
     * @param graph The graph to execute.
     * @param <T> The task type.
     * @param <E> The edge type.
     * @throws ExecutionException If there is an error executing one or more tasks.
     * @throws InterruptedException If task execution is interrupted.
     */
    public abstract <T extends Callable<?>,E> void execute(DAGNode<T,E> graph) throws ExecutionException, InterruptedException;
}
