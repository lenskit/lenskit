package org.grouplens.lenskit.util.parallel;

import com.google.common.base.Preconditions;
import org.grouplens.grapht.graph.DAGNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Multithreaded task graph executor.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ParallelTaskGraphExecutor extends TaskGraphExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ParallelTaskGraphExecutor.class);
    private final int threadCount;
    private final String name;

    ParallelTaskGraphExecutor(int nthreads, String n) {
        Preconditions.checkArgument(nthreads > 0, "thread count is not positive");
        threadCount = nthreads;
        name = n;
    }

    @Override
    public <T extends Callable<?>,E> void execute(DAGNode<T, E> graph) throws ExecutionException {
        logger.info("{}: executing {} tasks on {} threads", name,
                    graph.getReachableNodes().size(), threadCount);
        TaskGraphManager manager = new TaskGraphManager<T,E>(name, graph);
        for (int i = 1; i <= threadCount; i++) {
            Thread thread = new TaskGraphThread<T,E>(manager, String.format("%s-%d", name, i));
            thread.start();
        }
        manager.waitForFinished();
    }
}
