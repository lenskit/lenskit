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
package org.grouplens.lenskit.eval.graph;

import com.google.common.base.Preconditions;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.*;
import org.grouplens.lenskit.core.GraphtUtils;
import org.grouplens.lenskit.core.Parameter;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Class to manage traversing nodes. It is not used to handle the root node, but rather handles
 * the rest of them.
*/
class GraphDumper {
    private static final Logger logger = LoggerFactory.getLogger(GraphDumper.class);
    private static final String ROOT_ID = "root";

    private final GraphWriter writer;
    private final Graph graph;
    private final Set<Node> unsharedNodes;
    private final Map<Node, String> nodeIds;
    private final Map<String, String> nodeTargets;
    private final Queue<GVEdge> edgeQueue;

    public GraphDumper(Graph g, Set<Node> unshared, GraphWriter gw) {
        writer = gw;
        graph = g;
        unsharedNodes = new HashSet<Node>(unshared);
        unsharedNodes.retainAll(g.getNodes());
        logger.debug("{} shared nodes", unsharedNodes.size());
        nodeIds = new HashMap<Node, String>();
        nodeTargets = new HashMap<String, String>();
        edgeQueue = new LinkedList<GVEdge>();
    }

    /**
     * Set the root node for this dumper. This must be called before any other methods.
     * @param root The root node.
     * @return The ID of the root node.
     */
    public String setRoot(Node root) {
        if (!nodeTargets.isEmpty()) {
            throw new IllegalStateException("root node already specificied");
        }
        nodeIds.put(root, ROOT_ID);
        nodeTargets.put(ROOT_ID, ROOT_ID);
        writer.putNode(new NodeBuilder(ROOT_ID).setLabel("root")
                                               .setShape("box")
                                               .add("style", "rounded")
                                               .build());
        return ROOT_ID;
    }

    /**
     * Process a node.
     * @param node The node to process
     * @return The node's target descriptor (ID, possibly with port).
     */
    public String process(Node node) {
        Preconditions.checkNotNull(node, "node must not be null");
        if (nodeTargets.isEmpty()) {
            throw new IllegalStateException("root node has not been set");
        }
        String id = nodeIds.get(node);
        String tgt;
        if (id == null) {
            id = "N" + nodeIds.size();
            nodeIds.put(node, id);
            CachedSatisfaction csat = node.getLabel();
            assert csat != null;
            Satisfaction sat = csat.getSatisfaction();
            tgt = sat.visit(new Visitor(node, id));
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
    public void finish() {
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
        private final Node node;
        private final String nodeId;
        private final Satisfaction satisfaction;

        private Visitor(Node nd, String id) {
            node = nd;
            nodeId = id;
            if (node == null) {
                throw new IllegalStateException("dumper not running");
            }
            CachedSatisfaction csat = node.getLabel();
            assert csat != null;
            satisfaction = csat.getSatisfaction();
        }

        @Override
        public String visitNull() {
            NodeBuilder nb = new NodeBuilder(nodeId);
            nb.setShape("ellipse");
            Class<?> type = satisfaction.getErasedType();
            if (DataAccessObject.class.isAssignableFrom(type)) {
                nb.setLabel("DAO");
            } else {
                nb.setLabel("null");
            }
            GVNode node = nb.build();
            writer.putNode(node);
            return node.getTarget();
        }

        @Override
        public String visitClass(Class<?> clazz) {
            return componentNode(clazz, null).getTarget();
        }

        @Override
        public String visitInstance(Object instance) {
            GVNode node = new NodeBuilder(nodeId)
                    .setLabel(instance.toString())
                    .setShape("ellipse")
                    .build();
            writer.putNode(node);
            return node.getId();
        }

        /**
         * Output the intermediate "provided" node from the current node.
         * @param pid The ID of the provider node for targeting the provision edge.
         * @return The ID of the provided node, for targeting dependency edges.
         */
        private String putProvidedNode(String pid) {
            ComponentNodeBuilder cnb;
            cnb = new ComponentNodeBuilder(nodeId, satisfaction.getErasedType());
            cnb.setShareable(GraphtUtils.isShareable(node))
               .setShared(!unsharedNodes.contains(node))
               .setIsProvided(true);
            GVNode node = cnb.build();
            writer.putNode(node);
            edgeQueue.add(new EdgeBuilder(node.getTarget(), pid)
                                  .set("style", "dotted")
                                  .set("dir", "back")
                                  .set("arrowhead", "vee")
                                  .build());
            return node.getTarget();
        }

        @Override
        public String visitProviderClass(Class<? extends Provider<?>> pclass) {
            String pid = nodeId + "P";
            GVNode pnode = componentNode(pclass, pid);
            return putProvidedNode(pnode.getTarget());
        }

        @Override
        public String visitProviderInstance(Provider<?> provider) {
            String pid = nodeId + "P";
            writer.putNode(new NodeBuilder(pid)
                                   .setLabel(provider.toString())
                                   .setShape("ellipse")
                                   .set("style", "dashed")
                                   .build());
            return putProvidedNode(pid);
        }

        private GVNode componentNode(Class<?> type, String pid) {
            String id = pid == null ? nodeId : pid;
            ComponentNodeBuilder bld = new ComponentNodeBuilder(id, type);
            bld.setShareable(pid == null && GraphtUtils.isShareable(node));
            bld.setShared(!unsharedNodes.contains(node));
            bld.setIsProvider(pid != null);
            for (Edge e: graph.getOutgoingEdges(node)) {
                Desire dep = e.getDesire();
                assert dep != null;
                Annotation q = dep.getInjectionPoint().getAttributes().getQualifier();
                Node targetNode = e.getTail();
                if (q != null && q.annotationType().getAnnotation(Parameter.class) != null) {
                    logger.debug("dumping parameter {}", q);
                    CachedSatisfaction tcsat = targetNode.getLabel();
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
                    String tid = process(targetNode);
                    String port = String.format("%s:%d", id, bld.getLastDependencyPort());
                    EdgeBuilder eb = new EdgeBuilder(port, tid)
                            .set("arrowhead", "vee");
                    if (GraphtUtils.desireIsTransient(dep)) {
                        eb.set("style", "dashed");
                    }
                    edgeQueue.add(eb.build());
                }
            }
            GVNode node = bld.build();
            writer.putNode(node);
            return node;
        }
    }
}
