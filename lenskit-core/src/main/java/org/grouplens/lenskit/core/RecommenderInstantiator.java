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
package org.grouplens.lenskit.core;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.lenskit.RecommenderBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Build a recommender engine.
 *
 * @since 1.2
 * @compat Experimental
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class RecommenderInstantiator {
    private static final Logger logger = LoggerFactory.getLogger(RecommenderInstantiator.class);
    private final InjectSPI spi;
    private final DAGNode<CachedSatisfaction, DesireChain> graph;

    static RecommenderInstantiator create(InjectSPI spi, DAGNode<CachedSatisfaction,DesireChain> g) {
        return new RecommenderInstantiator(spi, g);
    }

    public static RecommenderInstantiator forConfig(LenskitConfiguration config) throws RecommenderConfigurationException {
        return new RecommenderInstantiator(config.getSPI(), config.buildGraph());
    }

    private RecommenderInstantiator(InjectSPI spi, DAGNode<CachedSatisfaction, DesireChain> g) {
        this.spi = spi;
        graph = g;
    }

    /**
     * Get the graph from this instantiator.
     *
     * @return The graph.  This method returns a defensive copy, so modifying it will not modify the
     *         graph underlying this instantiator.
     */
    public DAGNode<CachedSatisfaction, DesireChain> getGraph() {
        return graph;
    }

    /**
     * Instantiate the recommender graph.  This requires the graph to have been resolved with a real
     * DAO instance, not just a class, if anything references the DAO.  Use {@link
     * LenskitConfiguration#buildGraph()} to get such
     * a graph.
     *
     * @return A new instantiated recommender graph.
     * @throws RecommenderBuildException If there is an error instantiating the graph.
     */
    public DAGNode<CachedSatisfaction,DesireChain> instantiate() throws RecommenderBuildException {
        final StaticInjector injector = new StaticInjector(spi, graph);
        return replaceShareableNodes(new Function<DAGNode<CachedSatisfaction,DesireChain>, DAGNode<CachedSatisfaction,DesireChain>>() {
            @Nullable
            @Override
            @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
            public DAGNode<CachedSatisfaction,DesireChain> apply(@Nullable DAGNode<CachedSatisfaction,DesireChain> node) {
                Preconditions.checkNotNull(node);
                assert node != null;
                if (node.getLabel().getSatisfaction().hasInstance()) {
                    return node;
                }
                Object obj = injector.instantiate(node);
                CachedSatisfaction label = node.getLabel();
                Satisfaction instanceSat;
                if (obj == null) {
                    instanceSat = spi.satisfyWithNull(label.getSatisfaction().getErasedType());
                } else {
                    instanceSat = spi.satisfy(obj);
                }
                CachedSatisfaction newLabel = new CachedSatisfaction(instanceSat, label.getCachePolicy());
                // build new node with replacement label
                DAGNodeBuilder<CachedSatisfaction,DesireChain> bld = DAGNode.newBuilder(newLabel);
                // retain all non-transient edges
                for (DAGEdge<CachedSatisfaction,DesireChain> edge: node.getOutgoingEdges()) {
                    if (!GraphtUtils.edgeIsTransient(edge)) {
                        bld.addEdge(edge.getTail(), edge.getLabel());
                    }
                }
                return bld.build();
            }
        });
    }

    /**
     * Simulate instantiating a graph.
     * @return The simulated graph.
     */
    public DAGNode<CachedSatisfaction,DesireChain> simulate() {
        return replaceShareableNodes(new Function<DAGNode<CachedSatisfaction,DesireChain>, DAGNode<CachedSatisfaction,DesireChain>>() {
            @Nullable
            @Override
            @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
            public DAGNode<CachedSatisfaction,DesireChain> apply(@Nullable DAGNode<CachedSatisfaction,DesireChain> node) {
                Preconditions.checkNotNull(node);
                assert node != null;
                CachedSatisfaction label = node.getLabel();
                if (!label.getSatisfaction().hasInstance()) {
                    Satisfaction instanceSat = spi.satisfyWithNull(label.getSatisfaction().getErasedType());
                    CachedSatisfaction newLbl = new CachedSatisfaction(instanceSat, label.getCachePolicy());
                    // build new node with replacement label
                    DAGNodeBuilder<CachedSatisfaction,DesireChain> bld = DAGNode.newBuilder(newLbl);
                    // retain all non-transient edges
                    for (DAGEdge<CachedSatisfaction,DesireChain> edge: node.getOutgoingEdges()) {
                        if (!GraphtUtils.edgeIsTransient(edge)) {
                            bld.addEdge(edge.getTail(), edge.getLabel());
                        }
                    }
                    DAGNode<CachedSatisfaction,DesireChain> repl = bld.build();
                    logger.debug("simulating instantiation of {}", node);
                    return repl;
                } else {
                    return node;
                }
            }
        });
    }

    /**
     * Replace shareable nodes in a graph.
     *
     * @param replace   A replacement function. For each shareable node, this function will
     *                  be called; if it returns a node different from its input node, the
     *                  new node will be used as a replacement for the old.
     */
    private DAGNode<CachedSatisfaction,DesireChain> replaceShareableNodes(Function<DAGNode<CachedSatisfaction,DesireChain>,DAGNode<CachedSatisfaction,DesireChain>> replace) {
        logger.debug("replacing nodes in graph with {} nodes", graph.getReachableNodes().size());
        Set<DAGNode<CachedSatisfaction,DesireChain>> toReplace = getShareableNodes(graph);
        logger.debug("found {} shared nodes", toReplace.size());

        Map<DAGNode<CachedSatisfaction,DesireChain>,DAGNode<CachedSatisfaction,DesireChain>> memory = Maps.newHashMap();
        DAGNode<CachedSatisfaction,DesireChain> newGraph = graph;
        for (DAGNode<CachedSatisfaction,DesireChain> node : toReplace) {
            // look up this node in case it's already been replaced due to edge modifications
            while (memory.containsKey(node)) {
                node = memory.get(node);
            }

            // now look up and replace this node
            DAGNode<CachedSatisfaction,DesireChain> repl = replace.apply(node);
            if (repl != node) {
                newGraph = newGraph.replaceNode(node, repl, memory);
            }
        }

        logger.debug("final graph has {} nodes", newGraph.getReachableNodes().size());
        return newGraph;
    }

    /**
     * Find the set of shareable nodes (objects that will be replaced with instance satisfactions in
     * the final graph).
     *
     * @param graph The graph to analyze.
     * @return The set of root nodes - nodes that need to be instantiated and removed. These nodes
     *         are in topologically sorted order.
     */
    private LinkedHashSet<DAGNode<CachedSatisfaction, DesireChain>> getShareableNodes(DAGNode<CachedSatisfaction, DesireChain> graph) {
        LinkedHashSet<DAGNode<CachedSatisfaction, DesireChain>> shared = Sets.newLinkedHashSet();

        List<DAGNode<CachedSatisfaction, DesireChain>> nodes = graph.getSortedNodes();
        for (DAGNode<CachedSatisfaction, DesireChain> node : nodes) {
            if (!GraphtUtils.isShareable(node)) {
                continue;
            }

            // see if we depend on any non-shared nodes
            // since nodes are sorted, all shared nodes will have been seen
            boolean isShared = true;
            for (DAGEdge<CachedSatisfaction,DesireChain> edge: node.getOutgoingEdges()) {
                if (!GraphtUtils.edgeIsTransient(edge)) {
                    isShared &= shared.contains(edge.getTail());
                }
            }
            if (isShared) {
                shared.add(node);
            }
        }

        return shared;
    }
}
