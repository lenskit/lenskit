/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.inject;

import com.google.common.collect.Maps;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
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
                                                             NodeProcessor proc) throws InjectionException {
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
