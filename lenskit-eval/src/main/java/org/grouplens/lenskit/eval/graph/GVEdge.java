package org.grouplens.lenskit.eval.graph;

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
        attributes = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(attrs));
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
