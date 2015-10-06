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
package org.lenskit.graph;

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
