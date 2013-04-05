package org.grouplens.lenskit.eval.graph;

import groovy.json.StringEscapeUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Write a graph in GraphViz format.
 */
class GraphWriter implements Closeable {
    private static final Pattern SAFE_VALUE = Pattern.compile("\\w+");
    private final PrintWriter output;

    public GraphWriter(Writer out) {
        output = new PrintWriter(out);
        output.println("digraph {");
        output.println("  node [fontname=\"Helvetica\"]");
    }

    @Override
    public void close() throws IOException {
        output.println("}");
        output.close();
    }

    private String safeValue(Object obj) {
        String str = obj.toString();
        if (obj instanceof HTMLLabel || SAFE_VALUE.matcher(str).matches()) {
            return str;
        } else {
            return "\"" + StringEscapeUtils.escapeJava(str) + "\"";
        }
    }

    private void putAttributes(Map<String, Object> attrs) {
        if (!attrs.isEmpty()) {
            output.append(" [");
            boolean first = true;
            for (Map.Entry<String,Object> a: attrs.entrySet()) {
                if (!first) {
                    output.append(", ");
                }
                output.append(a.getKey())
                      .append("=")
                      .append(safeValue(a.getValue()));
                first = false;
            }
            output.append("]");
        }
    }

    public void putNode(GVNode node) {
        final String id = node.getId();
        output.append("  ")
              .append(id);
        putAttributes(node.getAttributes());
        output.append(";\n");
    }

    public void putEdge(GVEdge edge) {
        final String src = edge.getSource();
        final String dst = edge.getTarget();
        output.append("  ")
              .append(src)
              .append(" -> ")
              .append(dst);
        putAttributes(edge.getAttributes());
        output.append(";\n");
    }
}
