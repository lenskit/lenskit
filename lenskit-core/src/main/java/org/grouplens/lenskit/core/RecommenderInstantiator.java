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
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.lenskit.RecommenderBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

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
    private final Graph graph;

    static RecommenderInstantiator create(InjectSPI spi, Graph g) {
        return new RecommenderInstantiator(spi, g);
    }

    public static RecommenderInstantiator forConfig(LenskitConfiguration config) throws RecommenderConfigurationException {
        return new RecommenderInstantiator(config.getSPI(), config.buildGraph());
    }

    private RecommenderInstantiator(InjectSPI spi, Graph g) {
        this.spi = spi;
        graph = g;
    }

    /**
     * Get the graph from this instantiator.
     *
     * @return The graph.  This method returns a defensive copy, so modifying it will not modify the
     *         graph underlying this instantiator.
     */
    public Graph getGraph() {
        return graph.clone();
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
    public Graph instantiate() throws RecommenderBuildException {
        final StaticInjector injector = new StaticInjector(spi, graph);
        return replaceShareableNodes(new Function<Node, Node>() {
            @Nullable
            @Override
            @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
            public Node apply(@Nullable Node node) {
                Preconditions.checkNotNull(node);
                assert node != null;
                Object obj = injector.instantiate(node);
                CachedSatisfaction label = node.getLabel();
                assert label != null;
                Satisfaction instanceSat;
                if (obj == null) {
                    instanceSat = spi.satisfyWithNull(label.getSatisfaction().getErasedType());
                } else {
                    instanceSat = spi.satisfy(obj);
                }
                return new Node(instanceSat, label.getCachePolicy());
            }
        });
    }

    /**
     * Simulate instantiating a graph.
     * @return The simulated graph.
     */
    public Graph simulate() {
        return replaceShareableNodes(new Function<Node, Node>() {
            @Nullable
            @Override
            @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
            public Node apply(@Nullable Node node) {
                Preconditions.checkNotNull(node);
                assert node != null;
                CachedSatisfaction label = node.getLabel();
                assert label != null;
                if (!label.getSatisfaction().hasInstance()) {
                    Satisfaction instanceSat = spi.satisfyWithNull(label.getSatisfaction().getErasedType());
                    Node repl = new Node(instanceSat, label.getCachePolicy());
                    logger.debug("simulating instantiation of {}", node);
                    return repl;
                } else {
                    return node;
                }
            }
        });
    }

    /**
     * Instantiate the shared objects in a graph. This instantiates all shared objects,
     * and replaces their nodes with nodes wrapping instance satisfactions.
     *
     * @param replace   A replacement function. For each shareable node, this function will
     *                  be called; if it returnes a node different from its input node, the
     *                  new node will be used as a replacement for the old.
     */
    private Graph replaceShareableNodes(Function<Node,Node> replace) {
        Graph modified = graph.clone();
        Set<Node> toReplace = getShareableNodes(modified);
        logger.debug("found {} shared nodes", toReplace.size());
        LinkedHashSet<Node> replacements = new LinkedHashSet<Node>();
        for (Node node : toReplace) {
            Node repl = replace.apply(node);
            if (repl != node) {
                modified.replaceNode(node, repl);
                replacements.add(repl);
            }
        }
        // Remove transient edges and orphaned subgraphs
        Set<Node> transientTargets = removeTransientEdges(modified, replacements);
        Set<Node> removed = removeOrphanSubgraphs(modified, transientTargets);
        logger.debug("removed {} orphaned nodes", removed.size());
        return modified;
    }

    /**
     * Prune the graph, returning the set of nodes for shareable objects
     * (objects that will be replaced with instance satisfactions in the
     * final graph).
     *
     * @param graph The graph to analyze. The graph is not modified.
     * @return The set of root nodes - nodes that need to be instantiated and
     *         removed. These nodes are in topologically sorted order.
     */
    private LinkedHashSet<Node> getShareableNodes(Graph graph) {
        LinkedHashSet<Node> shared = new LinkedHashSet<Node>();

        List<Node> nodes = graph.sort(graph.getNode(null));
        for (Node node : nodes) {
            if (!GraphtUtils.isShareable(node)) {
                continue;
            }

            // see if we depend on any non-shared nodes
            // since nodes are sorted, all shared nodes will have been seen
            Set<Edge> intransient = GraphtUtils.removeTransient(graph.getOutgoingEdges(node));
            boolean isShared =
                    Iterables.all(Iterables.transform(intransient, GraphtUtils.edgeTail()),
                                  Predicates.in(shared));
            if (isShared) {
                shared.add(node);
            }
        }

        return shared;
    }

    /**
     * Remove transient edges from a graph.
     *
     * @param graph The graph to remove transient edges from.
     * @param nodes The nodes whose outgoing transient edges should be removed.
     * @return The set of tail nodes of removed edges.
     */
    private Set<Node> removeTransientEdges(Graph graph, Set<Node> nodes) {
        // Tail nodes of removed edges (return value)
        Set<Node> targets = new HashSet<Node>();
        // Nodes we have seen in our traversal (set of members of the queue)
        Set<Node> seen = new HashSet<Node>(nodes);
        // The work queue
        Queue<Node> work = new ArrayDeque<Node>(nodes);
        // Queue of removals, to avoid concurrent modification
        Queue<Edge> removals = new ArrayDeque<Edge>();

        // Pump the work queue
        while (!work.isEmpty()) {
            Node node = work.remove();
            // find and queue removals
            for (Edge e : graph.getOutgoingEdges(node)) {
                Node nbr = e.getTail();

                // remove transient edges, traverse non-transient ones
                Desire desire = e.getDesire();
                assert desire != null;
                if (GraphtUtils.desireIsTransient(desire)) {
                    removals.add(e);
                    targets.add(nbr);
                } else if (!seen.contains(nbr)) {
                    seen.add(nbr);
                    work.add(nbr);
                }
            }

            // process removals
            while (!removals.isEmpty()) {
                graph.removeEdge(removals.remove());
            }
            // invariant: removals is empty
        }

        return targets;
    }

    private Set<Node> removeOrphanSubgraphs(Graph graph, Collection<Node> candidates) {
        Set<Node> removed = new HashSet<Node>();
        Queue<Node> removeQueue = new LinkedList<Node>(candidates);
        while (!removeQueue.isEmpty()) {
            Node candidate = removeQueue.poll();
            Set<Edge> incoming = graph.getIncomingEdges(candidate); // null if candidate got re-added
            if (incoming != null && incoming.isEmpty()) {
                // No other node depends on this node, so we can remove it,
                // we must also flag its dependencies as removal candidates
                // Flag each multiple times, as it could become a candidate late
                for (Edge e : graph.getOutgoingEdges(candidate)) {
                    removeQueue.add(e.getTail());
                }
                logger.debug("removing orphan node {}", candidate);
                graph.removeNode(candidate);
                removed.add(candidate);
            }
        }
        return removed;
    }
}
