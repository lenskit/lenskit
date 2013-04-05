package org.grouplens.lenskit.eval.graph;

import org.apache.commons.lang3.builder.Builder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Build a graph node.
 */
class NodeBuilder implements Builder<GVNode> {
    private final String nodeId;
    private final Map<String,Object> attributes = new LinkedHashMap<String, Object>();
    private String target;

    /**
     * Construct a node builder for a specified ID.
     * @param id The node ID.
     */
    public NodeBuilder(String id) {
        nodeId = id;
        target = id;
    }

    /**
     * Set this node's target descriptor.
     * @param tgt The node's target descriptor.
     * @return The builder (for chaining).
     */
    public NodeBuilder setTarget(String tgt) {
        target = tgt;
        return this;
    }

    /**
     * Set a node attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return The builder (for chaining).
     */
    public NodeBuilder set(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    /**
     * Add to a node attribute.  This accumulates a comma-separated list in the attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return The builder (for chaining).
     */
    public NodeBuilder add(String name, Object value) {
        Object existing = attributes.get(name);
        if (existing == null) {
            return set(name, value);
        } else {
            return set(name, existing.toString() + "," + value.toString());
        }
    }

    /**
     * Set the label of the node.
     * @param label The node label. It will be wrapped in a string label and escaped.
     * @return The builder (for chaining).
     */
    public NodeBuilder setLabel(String label) {
        return set("label", label);
    }

    /**
     * Set the label of the node.
     * @param label The node label.
     * @return The builder (for chaining).
     */
    public NodeBuilder setLabel(HTMLLabel label) {
        return set("label", label);
    }

    public NodeBuilder setShape(String shape) {
        return set("shape", shape);
    }

    public NodeBuilder setFont(String font) {
        return set("fontname", font);
    }

    @Override
    public GVNode build() {
        return new GVNode(nodeId, attributes, target);
    }
}
