package org.grouplens.lenskit.inject;

import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;

import javax.annotation.Nonnull;

/**
 * Process a node in a graph.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public interface NodeProcessor {
    /**
     * Process a graph node.
     * @param node The node to process.
     * @param original The original node.  Some graph processing operations will change nodes prior
     *                 to processing due to changing some of their dependent nodes.  In such
     *                 operations, this parameter will receive the original node, while {@code node}
     *                 is the current node.
     * @return The processed node.
     */
    @Nonnull
    DAGNode<Component, Dependency> processNode(@Nonnull DAGNode<Component, Dependency> node,
                                               @Nonnull DAGNode<Component, Dependency> original);
}
