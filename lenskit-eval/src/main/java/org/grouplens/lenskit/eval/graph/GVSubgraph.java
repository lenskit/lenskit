/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
        attributes = Collections.unmodifiableMap(new LinkedHashMap<String,Object>(attrs));
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
