package org.grouplens.lenskit.eval.graph;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Graph representation for template rendering.
*/
class GraphRepr {
    List<NodeRepr> nodes;
    Map<Node,NodeRepr> nodeMap;
    List<Pair<String,String>> edges;

    public GraphRepr(Graph g) {
        nodes = new ArrayList<NodeRepr>();
        nodeMap = new HashMap<Node, NodeRepr>();
        edges = new ArrayList<Pair<String, String>>();
        int i = 0;
        for (Node node: g.getNodes()) {
            if (node.getLabel() == null) {
                continue;
            }
            NodeRepr repr = new NodeRepr(i, node, g.getOutgoingEdges(node));
            nodes.add(repr);
            nodeMap.put(node, repr);
        }
        // pass 2: edges
        for (Node node: g.getNodes()) {
            if (node.getLabel() == null) {
                continue;
            }
            NodeRepr srepr = nodeMap.get(node);
            for (Edge e: g.getOutgoingEdges(node)) {
                NodeRepr drepr = nodeMap.get(e.getTail());
                if (drepr == null) {
                    throw new RuntimeException("cannot find node " + e.getTail());
                }
                int port = srepr.edgePort(e);
                String src = port >= 0
                        ? String.format("%s:%d", srepr.getId(), port)
                        : srepr.getId();
                String dst = drepr.getId();
                edges.add(Pair.of(src, dst));
            }
        }
    }
}
