package org.grouplens.lenskit.eval.graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A graph node.
 */
class GVNode {
    private final String id;
    private final Map<String,Object> attributes;
    private final String target;

    /**
     * Construct a new graph node.
     * @param id The node ID.
     * @param attrs The node attributes.
     * @param tgt The node target (ID, possibly with port). Used for drawing edges to this
     *            node.
     */
    public GVNode(String id, Map<String, Object> attrs, String tgt) {
        this.id = id;
        attributes = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(attrs));
        target = tgt;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getTarget() {
        return target;
    }
}
