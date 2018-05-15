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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Write a graph in GraphViz format.
 */
class GraphWriter implements Closeable {
    private static final Pattern SAFE_VALUE = Pattern.compile("\\w+");
    /**
     * Escaper for GraphViz string literals. It is strange that they only escape quotes, not even
     * the escape character, but that is what they do.
     * See <a href="http://www.graphviz.org/content/dot-language">The DOT Language</a> for a
     * reference.
     */
    private static final Escaper GRAPHVIZ_ESCAPE =
            Escapers.builder()
                    .addEscape('"', "\\\"")
                    .build();

    private final BufferedWriter output;

    public GraphWriter(@WillCloseWhenClosed BufferedWriter out) throws IOException {
        output = out;
        output.append("digraph {");
        output.newLine();
        output.append("  node [fontname=\"Helvetica\"")
              .append(",color=\"").append(ComponentNodeBuilder.UNSHARED_BGCOLOR).append("\"")
              .append("];");
        output.newLine();
        output.append("  graph [rankdir=LR];");
        output.newLine();
        output.append("  edge [")
              .append("color=\"")
              .append(ComponentNodeBuilder.UNSHARED_BGCOLOR).append("\"")
              .append("];");
        output.newLine();
    }

    @Override
    public void close() throws IOException {
        // write the end of the GraphViz file, closing the writer when we're done
        try (BufferedWriter w = output) {
            w.write("}");
            w.newLine();
        }
    }

    private String safeValue(Object obj) {
        String str = obj != null ? obj.toString() : "(null)";
        if (obj instanceof HTMLLabel || SAFE_VALUE.matcher(str).matches()) {
            return str;
        } else {
            return "\"" + GRAPHVIZ_ESCAPE.escape(str) + "\"";
        }
    }

    private void putAttributes(Map<String, Object> attrs) throws IOException {
        if (!attrs.isEmpty()) {
            output.append(" [");
            Joiner.on(", ")
                  .withKeyValueSeparator("=")
                  .appendTo(output, Maps.transformValues(attrs, new Function<Object, String>() {
                      @Nullable
                      @Override
                      public String apply(@Nullable Object input) {
                          return safeValue(input);
                      }
                  }));
            output.append("]");
        }
    }

    public void putNode(GVNode node) throws IOException {
        final String id = node.getId();
        output.append("  ")
              .append(id);
        putAttributes(node.getAttributes());
        output.append(";");
        output.newLine();
    }

    public void putEdge(GVEdge edge) throws IOException {
        final String src = edge.getSource();
        final String dst = edge.getTarget();
        output.append("  ")
              .append(src)
              .append(" -> ")
              .append(dst);
        putAttributes(edge.getAttributes());
        output.append(";");
        output.newLine();
    }

    public void putSubgraph(GVSubgraph subgraph) throws IOException {
        output.append("  subgraph ");
        String name = subgraph.getName();
        if (name != null) {
            output.append(name).append(" ");
        }
        output.append("{");
        output.newLine();
        for (Map.Entry<String,Object> e: subgraph.getAttributes().entrySet()) {
            output.append("    ")
                  .append(e.getKey())
                  .append("=")
                  .append(safeValue(e.getValue()))
                  .append(";");
            output.newLine();
        }
        for (GVNode node: subgraph.getNodes()) {
            putNode(node);
        }
        for (GVEdge edge: subgraph.getEdges()) {
            putEdge(edge);
        }
        output.append("  }");
        output.newLine();
    }
}
