package org.grouplens.lenskit.inject;

import com.google.common.collect.Maps;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;

import java.util.Collection;
import java.util.Map;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class NodeProcessors {
    /**
     * Create a node processor that will instantiate nodes.  It will return nodes whose satisfactions
     * have been replaced with instance satisfactions containing the instance.
     *
     * @return The node processor.
     */
    public static NodeProcessor instantiate() {
        return instantiate(NodeInstantiator.create());
    }

    /**
     * Create a node processor that will instantiate nodes.  It will return nodes whose satisfactions
     * have been replaced with instance satisfactions containing the instance.
     *
     * @param inst The node instantiator to use when instantiating nodes.
     * @return The node processor.
     */
    public static NodeProcessor instantiate(NodeInstantiator inst) {
        return new InstantiatingNodeProcessor(inst);
    }

    /**
     * Create a node processor that will simulate instantiating nodes.
     * @return The node processor.
     */
    public static NodeProcessor simulateInstantiation() {
        return new SimulationNodeProcessor();
    }

    /**
     * Process a set of nodes in a graph using a node processor.  Each node is replaced by the
     * result of processing it with a {@link NodeProcessor}.  If a node changes because one of its
     * adjacent nodes was replaced, the original is passed as the {@code original} parameter to
     * {@link NodeProcessor#processNode(DAGNode, DAGNode)}.
     *
     * @param graph The graph to process.
     * @param toReplace The nodes in the graph to process.
     * @param proc The processor to use.
     * @return The processed graph.
     */
    public static DAGNode<Component,Dependency> processNodes(DAGNode<Component, Dependency> graph,
                                                             Collection<DAGNode<Component, Dependency>> toReplace,
                                                             NodeProcessor proc) {
        Map<DAGNode<Component,Dependency>,DAGNode<Component,Dependency>> memory = Maps.newHashMap();
        DAGNode<Component, Dependency> newGraph = graph;
        for (DAGNode<Component,Dependency> original : toReplace) {
            // look up this node in case it's already been replaced due to edge modifications
            DAGNode<Component, Dependency> node = original;
            while (memory.containsKey(node)) {
                node = memory.get(node);
            }

            // now look up and replace this node
            DAGNode<Component,Dependency> repl = proc.processNode(node, original);
            if (repl != node) {
                newGraph = newGraph.replaceNode(node, repl, memory);
            }
        }

        return newGraph;
    }
}
