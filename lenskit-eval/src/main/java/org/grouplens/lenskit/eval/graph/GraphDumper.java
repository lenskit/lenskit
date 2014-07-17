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
package org.grouplens.lenskit.eval.graph;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closer;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.AbstractSatisfactionVisitor;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.SatisfactionVisitor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.Parameter;
import org.grouplens.lenskit.inject.GraphtUtils;
import org.grouplens.lenskit.inject.RecommenderInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Class to manage traversing nodes. It is not used to handle the root node, but rather handles
 * the rest of them.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
*/
public class GraphDumper {
    private static final Logger logger = LoggerFactory.getLogger(GraphDumper.class);
    private static final String ROOT_ID = "root";

    private final GraphWriter writer;
    private final DAGNode<Component, Dependency> graph;
    private final HashSet<DAGNode<Component, Dependency>> unsharedNodes;
    private final Map<DAGNode<Component, Dependency>, String> nodeIds;
    private final Map<String, String> nodeTargets;
    private final Queue<GVEdge> edgeQueue;

    GraphDumper(DAGNode<Component, Dependency> g, Set<DAGNode<Component, Dependency>> unshared, GraphWriter gw) {
        writer = gw;
        graph = g;
        unsharedNodes = Sets.newHashSet(unshared);
        unsharedNodes.retainAll(g.getReachableNodes());
        logger.debug("{} shared nodes", unsharedNodes.size());
        nodeIds = new HashMap<DAGNode<Component, Dependency>, String>();
        nodeTargets = new HashMap<String, String>();
        edgeQueue = new LinkedList<GVEdge>();
    }

    /**
     * Set the root node for this dumper. This must be called before any other methods.
     *
     * @param root The root node.
     * @return The ID of the root node.
     */
    String setRoot(DAGNode<Component, Dependency> root) throws IOException {
        if (!nodeTargets.isEmpty()) {
            throw new IllegalStateException("root node already specificied");
        }
        nodeIds.put(root, ROOT_ID);
        nodeTargets.put(ROOT_ID, ROOT_ID);
        writer.putNode(NodeBuilder.create(ROOT_ID)
                                  .setLabel("root")
                                  .setShape("box")
                                  .add("style", "rounded")
                                  .build());
        return ROOT_ID;
    }

    /**
     * Process a node.
     *
     * @param node The node to process
     * @return The node's target descriptor (ID, possibly with port).
     */
    String process(DAGNode<Component, Dependency> node) throws IOException {
        Preconditions.checkNotNull(node, "node must not be null");
        if (nodeTargets.isEmpty()) {
            throw new IllegalStateException("root node has not been set");
        }
        String id = nodeIds.get(node);
        String tgt;
        if (id == null) {
            id = "N" + nodeIds.size();
            nodeIds.put(node, id);
            Component csat = node.getLabel();
            assert csat != null;
            Satisfaction sat = csat.getSatisfaction();
            try {
                tgt = sat.visit(new Visitor(node, id));
            } catch (RuntimeException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else {
                    throw e;
                }
            }
            Preconditions.checkNotNull(tgt, "the returned target was null");
            nodeTargets.put(id, tgt);
        } else {
            tgt = nodeTargets.get(id);
            if (tgt == null) {
                // tentatively use the node ID, we might remap it later
                tgt = id;
            }
        }
        return tgt;
    }

    /**
     * Finish the graph, writing the edges.
     */
    void finish() throws IOException {
        while (!edgeQueue.isEmpty()) {
            GVEdge e = edgeQueue.remove();
            String newTarget = nodeTargets.get(e.getTarget());
            if (newTarget != null) {
                e = EdgeBuilder.of(e).setTarget(newTarget).build();
            }
            writer.putEdge(e);
        }
    }

    private class Visitor implements SatisfactionVisitor<String> {
        private final DAGNode<Component, Dependency> currentNode;
        private final String nodeId;
        private final Satisfaction satisfaction;

        private Visitor(DAGNode<Component, Dependency> nd, String id) {
            currentNode = nd;
            nodeId = id;
            if (currentNode == null) {
                throw new IllegalStateException("dumper not running");
            }
            Component csat = currentNode.getLabel();
            assert csat != null;
            satisfaction = csat.getSatisfaction();
        }

        @Override
        public String visitNull() {
            NodeBuilder nb = NodeBuilder.create(nodeId);
            nb.setShape("ellipse");
            nb.setLabel("null");
            GVNode node = nb.build();
            try {
                writer.putNode(node);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
            return node.getTarget();
        }

        @Override
        public String visitClass(Class<?> clazz) {
            GVNode node = componentNode(clazz, null);
            try {
                writer.putNode(node);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
            return node.getTarget();
        }

        @Override
        public String visitInstance(Object instance) {
            GVNode node = NodeBuilder.create(nodeId)
                                     .setLabel(instance.toString())
                                     .setShape("ellipse")
                                     .build();
            try {
                writer.putNode(node);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
            return node.getId();
        }

        /**
         * Create a provided node from the current node, and queue an edge for it.
         *
         * @param pid The ID of the provider node for targeting the provision edge.
         * @return The provided node and the edge connect it to the provider node.
         */
        private Pair<GVNode, GVEdge> providedNode(String pid) {
            GVNode pNode = ComponentNodeBuilder.create(nodeId, satisfaction.getErasedType())
                                               .setShareable(GraphtUtils.isShareable(currentNode))
                                               .setShared(!unsharedNodes.contains(currentNode))
                                               .setIsProvided(true)
                                               .build();
            GVEdge pEdge = EdgeBuilder.create(pNode.getTarget() + ":e", pid)
                                      .set("style", "dotted")
                                      .set("dir", "back")
                                      .set("arrowhead", "vee")
                                      .build();
            return Pair.of(pNode, pEdge);
        }

        @Override
        public String visitProviderClass(Class<? extends Provider<?>> pclass) {
            String pid = nodeId + "P";
            // we create a comp. node for the provider, and a provided node for its target
            GVNode pnode = componentNode(pclass, pid);
            Pair<GVNode, GVEdge> provided = providedNode(pid);
            try {
                SubgraphBuilder sgb = new SubgraphBuilder();
                writer.putSubgraph(sgb.setName("sgp_" + pid)
                                      .addNode(pnode)
                                      .addNode(provided.getLeft())
                                      .addEdge(provided.getRight())
                                      .build());
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
            // return *provided* node's ID
            return provided.getLeft().getTarget();
        }

        @Override
        public String visitProviderInstance(Provider<?> provider) {
            String pid = nodeId + "P";
            GVNode pnode = NodeBuilder.create(pid)
                                      .setLabel(provider.toString())
                                      .setShape("ellipse")
                                      .set("style", "dashed")
                                      .build();
            Pair<GVNode, GVEdge> provided = providedNode(pid);
            try {
                SubgraphBuilder sgb = new SubgraphBuilder();
                writer.putSubgraph(sgb.setName("sgp_" + pid)
                                      .addNode(pnode)
                                      .addNode(provided.getLeft())
                                      .addEdge(provided.getRight())
                                      .build());
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
            // return *provided* node's ID
            return provided.getLeft().getTarget();
        }

        private GVNode componentNode(Class<?> type, String pid) {
            String id = pid == null ? nodeId : pid;
            ComponentNodeBuilder bld = ComponentNodeBuilder.create(id, type);
            bld.setShareable(pid == null && GraphtUtils.isShareable(currentNode))
               .setShared(!unsharedNodes.contains(currentNode))
               .setIsProvider(pid != null);
            List<DAGEdge<Component, Dependency>> edges = Lists.newArrayList(currentNode.getOutgoingEdges());
            Collections.sort(edges, GraphtUtils.DEP_EDGE_ORDER);
            for (DAGEdge<Component, Dependency> e: edges) {
                Desire dep = e.getLabel().getInitialDesire();
                Annotation q = dep.getInjectionPoint().getQualifier();
                DAGNode<Component, Dependency> targetNode = e.getTail();
                if (q != null && q.annotationType().getAnnotation(Parameter.class) != null) {
                    logger.debug("dumping parameter {}", q);
                    Component tcsat = targetNode.getLabel();
                    assert tcsat != null;
                    Satisfaction tsat = tcsat.getSatisfaction();
                    Object val = tsat.visit(new AbstractSatisfactionVisitor<Object>(null) {
                        @Override
                        public Object visitInstance(Object instance) {
                            return instance;
                        }
                    });
                    if (val == null) {
                        logger.warn("parameter {} not bound", q);
                    }
                    bld.addParameter(q, val);
                } else {
                    logger.debug("dumping dependency {}", dep);
                    bld.addDependency(dep);
                    String tid = null;
                    try {
                        tid = process(targetNode);
                    } catch (IOException exc) {
                        throw Throwables.propagate(exc);
                    }
                    String port = String.format("%s:%d", id, bld.getLastDependencyPort());
                    EdgeBuilder eb = EdgeBuilder.create(port, tid)
                                                .set("arrowhead", "vee");
                    if (e.getLabel().isFixed()) {
                        eb.set("arrowtail", "crow");
                    }
                    if (GraphtUtils.desireIsTransient(dep)) {
                        eb.set("style", "dashed");
                    }
                    edgeQueue.add(eb.build());
                }
            }
            GVNode node = bld.build();
            return node;
        }
    }

    /**
     * Render a graph to a file.
     *
     * @param graph The graph to render.
     * @param graphvizFile The file to write the graph to.
     * @throws IOException
     */
    public static void renderGraph(DAGNode<Component,Dependency> graph,
                                   File graphvizFile) throws IOException, RecommenderBuildException {
        logger.debug("graph has {} nodes", graph.getReachableNodes().size());
        logger.debug("simulating instantiation");
        RecommenderInstantiator instantiator = RecommenderInstantiator.create(graph);
        DAGNode<Component, Dependency> unshared = instantiator.simulate();
        logger.debug("unshared graph has {} nodes", unshared.getReachableNodes().size());
        Closer close = Closer.create();
        try {
            Writer writer = close.register(new FileWriter(graphvizFile));
            GraphWriter gw = close.register(new GraphWriter(writer));
            GraphDumper dumper = new GraphDumper(graph, unshared.getReachableNodes(), gw);
            logger.debug("writing root node");
            String rid = dumper.setRoot(graph);
            // process each other node & add an edge
            for (DAGEdge<Component, Dependency> e: graph.getOutgoingEdges()) {
                DAGNode<Component, Dependency> target = e.getTail();
                Component csat = target.getLabel();
                if (!satIsNull(csat.getSatisfaction())) {
                    logger.debug("processing node {}", csat.getSatisfaction());
                    String id = dumper.process(target);
                    gw.putEdge(EdgeBuilder.create(rid, id)
                                          .set("arrowhead", "vee")
                                          .build());
                }
            }
            // and we're done
            dumper.finish();
        } catch (Throwable th) {
            throw close.rethrow(th);
        } finally {
            close.close();
        }
    }

    private static boolean satIsNull(Satisfaction sat) {
        return sat.visit(new AbstractSatisfactionVisitor<Boolean>(false) {
            @Override
            public Boolean visitNull() {
                return true;
            }
        });
    }
}
