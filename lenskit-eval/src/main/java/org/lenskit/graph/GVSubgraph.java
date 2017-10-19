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

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A subgraph in GraphViz.
 */
class GVSubgraph {
    private final String name;
    private final Map<String, Object> attributes;
    private final List<GVNode> nodes;
    private final List<GVEdge> edges;

    /**
     * Construct a new subgraph. Don't call this, use {@link SubgraphBuilder}.
     * @param attrs The attributes.
     * @param ns The nodes.
     * @param es The edges.
     */
    GVSubgraph(String name, Map<String,Object> attrs, List<GVNode> ns, List<GVEdge> es) {
        this.name = name;
        attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attrs));
        nodes = ImmutableList.copyOf(ns);
        edges = ImmutableList.copyOf(es);
    }

    /**
     * Get the subgraph's name.
     * @return The subgraph's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the subgraph's attributes.
     * @return The subgraph's attributes.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Get the subgraph's nodes.
     * @return The subgraph's nodes
     */
    public List<GVNode> getNodes() {
        return nodes;
    }

    /**
     * Get the subgraph's edges.
     * @return The subgraph's edges.
     */
    public List<GVEdge> getEdges() {
        return edges;
    }
}
