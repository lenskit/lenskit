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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A graph edge.
 */
class GVEdge {
    private final String source;
    private final String target;
    private final Map<String,Object> attributes;

    /**
     * Construct a new graph edge.
     * @param src The source node ID.
     * @param tgt The source node ID.
     * @param attrs The edge attributes.
     */
    public GVEdge(String src, String tgt, Map<String, Object> attrs) {
        source = src;
        target = tgt;
        attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attrs));
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
