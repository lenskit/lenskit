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
import org.apache.commons.lang3.builder.Builder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Build a graph node.
 */
class EdgeBuilder implements Builder<GVEdge> {
    private String srcId, tgtId;
    private final Map<String,Object> attributes;

    private EdgeBuilder(String src, String tgt) {
        Preconditions.checkNotNull(src, "source ID must not be null");
        Preconditions.checkNotNull(tgt, "target ID must not be null");
        srcId = src;
        tgtId = tgt;
        attributes = new LinkedHashMap<String, Object>();
    }

    private EdgeBuilder(String src, String tgt, Map<String,Object> attrs) {
        srcId = src;
        tgtId = tgt;
        attributes = new LinkedHashMap<String, Object>(attrs);
    }

    /**
     * Construct a node builder for a specified ID.
     * @param src The source node ID.
     * @param tgt The target node ID.
     */
    public static EdgeBuilder create(String src, String tgt) {
        return new EdgeBuilder(src, tgt);
    }

    public static EdgeBuilder of(GVEdge edge) {
        return new EdgeBuilder(edge.getSource(), edge.getTarget(), edge.getAttributes());
    }

    /**
     * Set a node attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return The builder (for chaining).
     */
    public EdgeBuilder set(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    public EdgeBuilder setSource(String src) {
        Preconditions.checkNotNull(src, "source ID must not be null");
        srcId = src;
        return this;
    }

    public EdgeBuilder setTarget(String tgt) {
        Preconditions.checkNotNull(tgt, "target ID must not be null");
        tgtId = tgt;
        return this;
    }

    @Override
    public GVEdge build() {
        return new GVEdge(srcId, tgtId, attributes);
    }
}
