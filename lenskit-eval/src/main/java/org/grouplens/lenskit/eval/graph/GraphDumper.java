package org.grouplens.lenskit.eval.graph;

import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.SatisfactionVisitor;

import javax.inject.Provider;
import java.util.*;

/**
 * Class to manage traversing nodes. It is not used to handle the root node, but rather handles
 * the rest of them.
*/
class GraphDumper implements SatisfactionVisitor<Void> {
    private static final String ROOT_ID = "root";

    private final GraphWriter writer;
    private final Graph graph;
    private final Map<Node, String> nodeIds;
    private final Queue<Node> queue;

    private Node currentNode;

    public GraphDumper(Graph g, GraphWriter gw) {
        writer = gw;
        graph = g;
        nodeIds = new HashMap<Node, String>();
        queue = new LinkedList<Node>();
    }

    /**
     * Set the root node for this dumper. This must be called before any other methods.
     * @param root The root node.
     * @return The ID of the root node.
     */
    public String setRoot(Node root) {
        if (!nodeIds.isEmpty()) {
            throw new IllegalStateException("root node already specificied");
        }
        nodeIds.put(root, ROOT_ID);
        writer.putRootNode(ROOT_ID);
        return ROOT_ID;
    }

    /**
     * Add a node to the queue.  If the node is already enqueued, the previous ID number
     * is returned.
     * @param node The node to enqueue
     * @return The node's ID.
     */
    public String enqueue(Node node) {
        if (nodeIds.isEmpty()) {
            throw new IllegalStateException("root node has not been set");
        }
        String id = nodeIds.get(node);
        if (id == null) {
            queue.add(node);
            id = "N" + nodeIds.size();
            nodeIds.put(node, id);
        }
        return id;
    }

    /**
     * Run the queue.
     */
    public void run() {
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            final CachedSatisfaction csat = currentNode.getLabel();
            assert csat != null;
            final Satisfaction sat = csat.getSatisfaction();
            sat.visit(this);
        }
        currentNode = null;
    }

    private String currentNodeId() {
        if (currentNode == null) {
            throw new IllegalStateException("dumper not running");
        }
        return nodeIds.get(currentNode);
    }

    private Satisfaction currentSatisfaction() {
        if (currentNode == null) {
            throw new IllegalStateException("dumper not running");
        }
        CachedSatisfaction csat = currentNode.getLabel();
        assert csat != null;
        return csat.getSatisfaction();
    }

    @Override
    public Void visitNull() {
        writer.putNullNode(currentNodeId(), currentSatisfaction().getErasedType());
        return null;
    }

    @Override
    public Void visitClass(Class<?> clazz) {
        putComponentNode(clazz, null);
        return null;
    }

    @Override
    public Void visitInstance(Object instance) {
        writer.putObjectNode(currentNodeId(), instance, false);
        return null;
    }

    @Override
    public Void visitProviderClass(Class<? extends Provider<?>> pclass) {
        writer.putTypeNode(currentNodeId(), currentSatisfaction().getType());
        String pid = currentNodeId() + "P";
        writer.putEdgeProvidedBy(currentNodeId(), pid);
        putComponentNode(pclass, pid);
        return null;
    }

    @Override
    public Void visitProviderInstance(Provider<?> provider) {
        writer.putTypeNode(currentNodeId(), currentSatisfaction().getType());
        String pid = currentNodeId() + "P";
        writer.putEdgeProvidedBy(currentNodeId(), pid);
        writer.putObjectNode(pid, provider, true);
        return null;
    }

    private void putComponentNode(Class<?> type, String pid) {
        String id = pid == null ? currentNodeId() : pid;
        List<Desire> desires = new ArrayList<Desire>();
        for (Edge e: graph.getOutgoingEdges(currentNode)) {
            desires.add(e.getDesire());
            Node t = e.getTail();
            String tid = enqueue(t);
            String port = String.format("%s:%d", id, desires.size());
            writer.putEdgeDepends(port, tid);
        }
        writer.putComponentNode(id, type, desires, pid != null);
    }
}
