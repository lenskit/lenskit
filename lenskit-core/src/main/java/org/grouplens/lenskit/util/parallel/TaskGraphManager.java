package org.grouplens.lenskit.util.parallel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.grouplens.grapht.graph.DAGNode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Work queue manager for task graph runner threads.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TaskGraphManager<T extends Callable<?>,E> {
    private volatile boolean finished = false;
    private volatile Throwable error = null;
    private final String name;
    private final List<DAGNode<T,E>> tasksToRun;
    private final Set<DAGNode<T,E>> finishedTasks;

    TaskGraphManager(String n, DAGNode<T,E> graph) {
        name = n;
        tasksToRun = Lists.newLinkedList(graph.getSortedNodes());
        finishedTasks = Sets.newHashSet();
    }

    public String getName() {
        return name;
    }

    /**
     * Get a runnable task.  Waits until one is available.
     * @return The next task to run, or {@code null} if there
     * are no more tasks to be run.
     */
    @Nullable
    public synchronized DAGNode<T,E> getRunnableTask() {
        boolean done = false;
        DAGNode<T, E> task = null;
        while (!done) {
            if (finished) {
                // nothing more to do
                done = true;
            } else {
                // is a task runnable?
                task = findRunnableTask();
                while (task == null && !tasksToRun.isEmpty()) {
                    // no, but there are tasks left, wait until one is returned
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        /* do nothing, loop through */
                    }
                    task = findRunnableTask();
                }
                if (task != null) {
                    // Look! A job!
                    done = true;
                    tasksToRun.remove(task);
                } else if (tasksToRun.isEmpty()) {
                    // We're out of jobs
                    done = true;
                } // otherwise try again and wait
            }
        }

        return task;
    }

    /**
     * Mark a task as finished.
     * @param task The completed task.
     * @param err The error (if the task failed), or {@code null} if the task completed
     *            successfully.
     */
    public synchronized void taskFinished(DAGNode<T,E> task, Throwable err) {
        finishedTasks.add(task);
        if (err != null && error == null) {
            error = err;
            finished = true;
        } else if (finishedTasks.containsAll(tasksToRun)) {
            finished = true;
        }
        // wake up anyone looking for a runnable task
        notifyAll();
    }

    public synchronized void waitForFinished() throws ExecutionException {
        while (!finished) {
            try {
                wait();
            } catch (InterruptedException e) {
                /* try again */
            }
        }
        if (error != null) {
            throw new ExecutionException("thread in group " + name + " failed", error);
        }
    }

    /**
     * Get the next runnable task, or {@code null} if no tasks can be run.
     * @return The runnable task.
     */
    @Nullable
    private DAGNode<T,E>  findRunnableTask() {
        for (DAGNode<T,E> task: tasksToRun) {
            if (finishedTasks.containsAll(task.getAdjacentNodes())) {
                return task;
            }
        }
        return null;
    }
}
