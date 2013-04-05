package org.grouplens.lenskit.eval.graph;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * An string label for a node.
 */
class StringLabel {
    private final String label;

    public StringLabel(String content) {
        label = content;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "\"" + StringEscapeUtils.escapeJava(label) + "\"";
    }
}
