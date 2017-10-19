/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.inject;

import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.LifecycleManager;
import org.grouplens.grapht.graph.DAGNode;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;
import org.lenskit.api.RecommenderBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Process a recommender graph to deal with its shareable nodes.
 *
 * @since 1.2
 * @compat Experimental
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class RecommenderInstantiator {
    private static final Logger logger = LoggerFactory.getLogger(RecommenderInstantiator.class);
    private final DAGNode<Component, Dependency> graph;

    public static RecommenderInstantiator create(DAGNode<Component,Dependency> g) {
        return new RecommenderInstantiator(g);
    }

    @Deprecated
    public static RecommenderInstantiator forConfig(LenskitConfiguration config) throws RecommenderConfigurationException {
        return create(config.buildGraph());
    }

    private RecommenderInstantiator(DAGNode<Component, Dependency> g) {
        graph = g;
    }

    /**
     * Get the graph from this instantiator.
     *
     * @return The graph.  This method returns a defensive copy, so modifying it will not modify the
     *         graph underlying this instantiator.
     */
    public DAGNode<Component, Dependency> getGraph() {
        return graph;
    }

    /**
     * Instantiate the recommender graph.  This requires the graph to have been resolved with a real
     * DAO instance, not just a class, if anything references the DAO.  Use {@link
     * LenskitConfiguration#buildGraph()} to get such a graph.  The result of instantiating a graph
     * is that all shareable nodes will be instantiated.
     *
     * @return A new recommender graph with all shareable nodes pre-instantiated.
     * @throws RecommenderBuildException If there is an error instantiating the graph.
     */
    public DAGNode<Component,Dependency> instantiate() throws RecommenderBuildException {
        try (LifecycleManager lm = new LifecycleManager()) {
            NodeInstantiator instantiator = NodeInstantiator.create(lm);
            // TODO Verify that no sharable components are lifecycle-managed
            return replaceShareableNodes(NodeProcessors.instantiate(instantiator));
        } catch (InjectionException e) {
            throw new RecommenderBuildException("Recommender instantiation failed", e);
        }
    }

    /**
     * Simulate instantiating a graph.
     * @return The simulated graph.
     */
    public DAGNode<Component,Dependency> simulate() throws RecommenderBuildException {
        try {
            return replaceShareableNodes(NodeProcessors.simulateInstantiation());
        } catch (InjectionException e) {
            throw new RecommenderBuildException("Simulated instantiation failed", e);
        }
    }

    /**
     * Replace shareable nodes in a graph.
     *
     * @param replace   A replacement function. For each shareable node, this function will
     *                  be called; if it returns a node different from its input node, the
     *                  new node will be used as a replacement for the old.
     */
    private DAGNode<Component,Dependency> replaceShareableNodes(NodeProcessor replace) throws InjectionException {
        logger.debug("replacing nodes in graph with {} nodes", graph.getReachableNodes().size());
        Set<DAGNode<Component,Dependency>> toReplace = GraphtUtils.getShareableNodes(graph);
        logger.debug("found {} shared nodes", toReplace.size());

        DAGNode<Component, Dependency> result =
                NodeProcessors.processNodes(graph, toReplace, replace);
        logger.debug("final graph has {} nodes", result.getReachableNodes().size());
        return result;
    }
}
