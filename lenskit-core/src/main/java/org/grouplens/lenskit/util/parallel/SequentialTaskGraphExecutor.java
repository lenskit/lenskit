package org.grouplens.lenskit.util.parallel;

import org.grouplens.grapht.graph.DAGNode;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Single-threaded (sequential) task graph executor.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SequentialTaskGraphExecutor extends TaskGraphExecutor {
    @Override
    public <T extends Callable<?>,E> void execute(DAGNode<T,E> graph) throws ExecutionException {
        for (DAGNode<T,?> node: graph.getSortedNodes()) {
            try {
                node.getLabel().call();
            } catch (Throwable th) {
                throw new ExecutionException("error in graph task", th);
            }
        }
    }
}
