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
package org.lenskit.graph;

import org.apache.commons.lang3.builder.Builder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Build a graph node.
 */
class NodeBuilder implements Builder<GVNode> {
    private final String nodeId;
    private final Map<String,Object> attributes = new LinkedHashMap<>();
    private String target;

    private NodeBuilder(String id) {
        nodeId = id;
        target = id;
    }

    /**
     * Construct a node builder for a specified ID.
     * @param id The node ID.
     */
    public static NodeBuilder create(String id) {
        return new NodeBuilder(id);
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
