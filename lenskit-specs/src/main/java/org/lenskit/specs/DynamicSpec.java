package org.lenskit.specs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * A specification that is entirely dynamic, containing an arbitrary JSON tree.
 */
public class DynamicSpec extends AbstractSpec {
    private JsonNode json;

    /**
     * Create a new null dynamic spec.
     */
    public DynamicSpec() {
        json = NullNode.getInstance();
    }

    /**
     * Create a new dynamic spec wrapper.
     * @param node The dynamic spec wrapper.
     */
    @JsonCreator
    public DynamicSpec(JsonNode node) {
        json = node;
    }

    /**
     * Get the JSON node in this spec.
     * @return The JSON node in this spec.
     */
    @JsonValue
    public JsonNode getJSON() {
        return json;
    }

    /**
     * Set the JSON node in this spec.
     * @param js The JSON node to wrap.
     */
    public void setJSON(JsonNode js) {
        json = js;
    }
}
