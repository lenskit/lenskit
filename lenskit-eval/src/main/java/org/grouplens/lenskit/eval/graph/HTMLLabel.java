package org.grouplens.lenskit.eval.graph;

/**
 * An HTML label for a node.
 */
class HTMLLabel {
    private final String label;

    public HTMLLabel(String content) {
        label = content;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "<" + label + ">";
    }
}
