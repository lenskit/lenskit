package org.grouplens.lenskit.inject;

import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.Satisfactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Node processor that simulates instantiation.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SimulationNodeProcessor implements NodeProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SimulationNodeProcessor.class);

    public DAGNode<Component, Dependency> processNode(@Nonnull DAGNode<Component, Dependency> node, @Nonnull DAGNode<Component, Dependency> original) {
        Component label = node.getLabel();
        if (!label.getSatisfaction().hasInstance()) {
            Satisfaction instanceSat = Satisfactions.nullOfType(label.getSatisfaction().getErasedType());
            Component newLbl = Component.create(instanceSat,
                                                label.getCachePolicy());
            // build new node with replacement label
            DAGNodeBuilder<Component,Dependency> bld = DAGNode.newBuilder(newLbl);
            // retain all non-transient edges
            for (DAGEdge<Component,Dependency> edge: node.getOutgoingEdges()) {
                if (!GraphtUtils.edgeIsTransient(edge)) {
                    bld.addEdge(edge.getTail(), edge.getLabel());
                }
            }
            DAGNode<Component,Dependency> repl = bld.build();
            logger.debug("simulating instantiation of {}", node);
            return repl;
        } else {
            return node;
        }
    }
}
