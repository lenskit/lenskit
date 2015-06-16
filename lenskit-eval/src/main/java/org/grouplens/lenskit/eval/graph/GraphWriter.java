/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval.graph;

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
        String str = obj.toString();
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
