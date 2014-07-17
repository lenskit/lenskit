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

import org.apache.commons.lang3.builder.Builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Build a subgraph for grouping attributes.
 */
class SubgraphBuilder implements Builder<GVSubgraph> {
    private final List<GVNode> nodes;
    private final List<GVEdge> edges;
    private final Map<String,Object> attributes;
    private String name;

    /**
     * Construct a subgraph builder.
     */
    public SubgraphBuilder() {
        nodes = new ArrayList<GVNode>();
        edges = new ArrayList<GVEdge>();
        attributes = new LinkedHashMap<String, Object>();
    }

    public SubgraphBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set a node attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return The builder (for chaining).
     */
    public SubgraphBuilder set(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    /**
     * Add a node to this subgraph.
     * @param node The node to add.
     * @return The subgraph builder (for chaining).
     */
    public SubgraphBuilder addNode(GVNode node) {
        nodes.add(node);
        return this;
    }

    /**
     * Add an edge to this subgraph.
     * @param edge The edge to add.
     * @return The subgraph builder (for chaining).
     */
    public SubgraphBuilder addEdge(GVEdge edge) {
        edges.add(edge);
        return this;
    }

    @Override
    public GVSubgraph build() {
        return new GVSubgraph(name, attributes, nodes, edges);
    }
}
