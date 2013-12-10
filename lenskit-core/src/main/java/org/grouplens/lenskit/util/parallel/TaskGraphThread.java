package org.grouplens.lenskit.util.parallel;

import org.grouplens.grapht.graph.DAGNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Thread for running task graph jobs.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TaskGraphThread<T extends Callable<?>,E> extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(TaskGraphThread.class);
    private final TaskGraphManager<T,E> manager;

    public TaskGraphThread(TaskGraphManager<T,E> mgr, String name) {
        super(name);
        manager = mgr;
    }

    public void run() {
        DAGNode<T,E> task = manager.getRunnableTask();
        while (task != null) {
            try {
                logger.debug("executing task {}", task.getLabel());
                task.getLabel().call();
                manager.taskFinished(task, null);
            } catch (Throwable th) {
                logger.error("error in task " + task.getLabel(), th);
                manager.taskFinished(task, th);
            }
            task = manager.getRunnableTask();
        }
    }
}
