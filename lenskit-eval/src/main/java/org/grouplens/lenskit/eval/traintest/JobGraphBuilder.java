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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Build job graphs.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class JobGraphBuilder {
    private static final Logger logger = LoggerFactory.getLogger(JobGraphBuilder.class);
    private final TrainTestEvalTask evalTask;
    private final ComponentCache componentCache;

    @Nullable
    private DAGNode<JobGraph.Node,JobGraph.Edge> previousRoot;
    @Nonnull
    private DAGNodeBuilder<JobGraph.Node,JobGraph.Edge> graphBuilder;

    private Map<DAGNode<Component,Dependency>, DAGNode<JobGraph.Node,JobGraph.Edge>> seenNodes;

    public JobGraphBuilder(@Nonnull TrainTestEvalTask task,
                           @Nullable ComponentCache cache) {
        evalTask = task;
        componentCache = cache;
        graphBuilder = DAGNode.newBuilder(JobGraph.noopNode("root"));
        seenNodes = Maps.newHashMap();
    }

    /**
     * Create a job fence - any previous jobs should be isolated from subsequent jobs.
     * @param name The fence name.
     */
    public void fence(String name) {
        graphBuilder.setLabel(JobGraph.noopNode(name));
        DAGNode<JobGraph.Node, JobGraph.Edge> node = graphBuilder.build();
        logger.debug("fencing {} nodes with {}", node.getReachableNodes().size(), name);
        assert previousRoot == null || node.getReachableNodes().contains(previousRoot);
        previousRoot = node;
        graphBuilder = DAGNode.newBuilder(JobGraph.noopNode("root"));
    }

    /**
     * Get the graph that has been built.
     * @return The graph that has been built.
     */
    public DAGNode<JobGraph.Node,JobGraph.Edge> getGraph() {
        if (graphBuilder.build().getOutgoingEdges().isEmpty() && previousRoot != null) {
            logger.debug("no new nodes since last fence, using fence root");
            return previousRoot;
        } else {
            DAGNode<JobGraph.Node, JobGraph.Edge> graph = graphBuilder.build();
            logger.debug("building graph with {} nodes",
                         graph.getReachableNodes().size());
            return graph;
        }
    }

    public void addLenskitJob(AlgorithmInstance algo, TTDataSet data,
                              DAGNode<Component,Dependency> graph) {
        logger.debug("adding node for algorithm {} on {}", algo, data);
        TrainTestJob job = new LenskitEvalJob(evalTask, algo, data, graph, componentCache);
        DAGNodeBuilder<JobGraph.Node,JobGraph.Edge> node = DAGNode.newBuilder(JobGraph.jobNode(job));
        if (previousRoot != null) {
            node.addEdge(previousRoot, JobGraph.edge());
        }
        addSharedNodeDependencies(graph, node);
        DAGNode<JobGraph.Node, JobGraph.Edge> jobNode = node.build();
        logger.debug("node has {} dependencies", jobNode.getAdjacentNodes().size());
        for (DAGNode<Component,Dependency> gn: graph.getReachableNodes()) {
            if (seenNodes.containsKey(gn)) {
                assert jobNode.getAdjacentNodes().contains(seenNodes.get(gn));
                if (componentCache != null) {
                    componentCache.registerSharedNode(gn);
                }
            } else {
                seenNodes.put(gn, jobNode);
            }
        }
        graphBuilder.addEdge(jobNode, JobGraph.edge());
    }

    private void addSharedNodeDependencies(DAGNode<Component, Dependency> graph,
                                           DAGNodeBuilder<JobGraph.Node, JobGraph.Edge> builder) {
        logger.debug("scanning for dependencies of {}", builder.build().getLabel());
        SetMultimap<DAGNode<JobGraph.Node,JobGraph.Edge>,DAGNode<Component,Dependency>> edges;
        edges = HashMultimap.create();
        for (DAGNode<Component, Dependency> node: graph.getReachableNodes()) {
            if (seenNodes.containsKey(node)) {
                edges.put(seenNodes.get(node), node);
            }
        }
        for (DAGNode<JobGraph.Node,JobGraph.Edge> dep: edges.keySet()) {
            if (logger.isDebugEnabled()) {
                logger.debug("depends on {} for {} nodes", dep, edges.get(dep).size());
                for (DAGNode<Component, Dependency> shared: edges.get(dep)) {
                    logger.debug("reuses {}", shared);
                }
            }
            builder.addEdge(dep, JobGraph.edge(edges.get(dep)));
        }
    }

    public void addExternalJob(ExternalAlgorithm algo, TTDataSet data) {
        logger.debug("adding node for algorithm {} on {}", algo, data);
        TrainTestJob job = new ExternalEvalJob(evalTask, algo, data);
        DAGNodeBuilder<JobGraph.Node,JobGraph.Edge> node = DAGNode.newBuilder(JobGraph.jobNode(job));
        if (previousRoot != null) {
            node.addEdge(previousRoot, JobGraph.edge());
        }
        graphBuilder.addEdge(node.build(), JobGraph.edge());
    }
}
