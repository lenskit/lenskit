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
        attributes = new LinkedHashMap<>();
    }

    private EdgeBuilder(String src, String tgt, Map<String,Object> attrs) {
        srcId = src;
        tgtId = tgt;
        attributes = new LinkedHashMap<>(attrs);
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
