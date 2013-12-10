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

    public abstract <T extends Callable<?>,E> void execute(DAGNode<T,E> graph) throws ExecutionException;
}
