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
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        attributes = new LinkedHashMap<>();
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
