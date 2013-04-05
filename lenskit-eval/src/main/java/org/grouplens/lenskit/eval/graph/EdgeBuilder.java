package org.grouplens.lenskit.eval.graph;

import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Build a graph node.
 */
class EdgeBuilder implements Builder<Pair<Pair<String,String>,Map<String,Object>>> {
    private final String srcId, tgtId;
    private final Map<String,Object> attributes = new LinkedHashMap<String, Object>();

    /**
     * Construct a node builder for a specified ID.
     * @param src The source node ID.
     * @param tgt The target node ID.
     */
    public EdgeBuilder(String src, String tgt) {
        srcId = src;
        tgtId = tgt;
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

    @Override
    public Pair<Pair<String, String>, Map<String, Object>> build() {
        return Pair.of(Pair.of(srcId, tgtId), Collections.unmodifiableMap(attributes));
    }
}
