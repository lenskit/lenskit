/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
import groovy.json.StringEscapeUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Write a graph in GraphViz format.
 */
class GraphWriter implements Closeable {
    private static final Pattern SAFE_VALUE = Pattern.compile("\\w+");
    private final BufferedWriter output;

    public GraphWriter(Writer out) throws IOException {
        output = new BufferedWriter(out);
        output.append("digraph {\n");
        output.append("  node [fontname=\"Helvetica\"")
              .append(",color=\"").append(ComponentNodeBuilder.UNSHARED_BGCOLOR).append("\"")
              .append("];\n");
        output.append("  edge [")
              .append("color=\"")
              .append(ComponentNodeBuilder.UNSHARED_BGCOLOR).append("\"")
              .append("];\n");
    }

    @Override
    public void close() throws IOException {
        output.write("}\n");
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
        output.append(";\n");
    }

    public void putEdge(GVEdge edge) throws IOException {
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
