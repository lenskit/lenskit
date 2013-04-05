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

    /**
     * Construct a node builder for a specified ID.
     * @param src The source node ID.
     * @param tgt The target node ID.
     */
    public EdgeBuilder(String src, String tgt) {
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
