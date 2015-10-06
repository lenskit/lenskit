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
package org.lenskit.graph;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.grapht.reflect.Desire;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

/**
 * Build a component label.
 */
class ComponentNodeBuilder implements Builder<GVNode> {
    static final String SHAREABLE_BGCOLOR = "#4e9a06";
    static final String SHAREABLE_UNSHARED_BGCOLOR = "#a40000";
    static final String UNSHARED_BGCOLOR = "#2e3436";
    static final String TYPE_COLOR = "white";
    static final String SHARED_COLOR = "#555753";
    static final String UNSHARED_COLOR = "black";

    private final String nodeId;
    private final String label;
    private final List<String> dependencies = new ArrayList<String>();
    private final List<String> parameters = new ArrayList<String>();
    private boolean shareable = false;
    private boolean isShared = false;
    private boolean isProvider = false;
    private boolean isProvided = false;
    private boolean isInterface = false;

    private ComponentNodeBuilder(String id, Class<?> type) {
        nodeId = id;
        label = shortClassName(type);
        isInterface = type.isInterface();
    }

    /**
     * Create a new component label builder.
     * @param id The node ID.
     * @param type The type of component to label.
     */
    public static ComponentNodeBuilder create(String id, Class<?> type) {
        return new ComponentNodeBuilder(id, type);
    }

    /**
     * Add a dependency of this component.
     * @param dep The dependency.
     * @return The builder (for chaining).
     */
    public ComponentNodeBuilder addDependency(Desire dep) {
        Annotation q = dep.getInjectionPoint().getQualifier();
        Class<?> type = dep.getDesiredType();
        if (q == null) {
            dependencies.add(shortClassName(type));
        } else {
            dependencies.add(shortAnnotation(q) + ": " + shortClassName(type));
        }
        return this;
    }

    public ComponentNodeBuilder addParameter(Annotation param, Object value) {
        StringBuilder lbl = new StringBuilder();
        lbl.append(shortAnnotation(param))
           .append(": ");
        if (value instanceof String) {
            lbl.append('"')
               .append(escapeJava(value.toString()))
               .append('"');
        } else {
            lbl.append(value);
        }
        parameters.add(lbl.toString());
        return this;
    }

    /**
     * Get the port of the last added dependency.
     * @return The port of the last added dependency.
     */
    public int getLastDependencyPort() {
        if (dependencies.isEmpty()) {
            throw new IllegalStateException("dependency list is empty");
        }
        return dependencies.size();
    }

    public ComponentNodeBuilder setShareable(boolean yn) {
        shareable = yn;
        return this;
    }

    public ComponentNodeBuilder setShared(boolean yn) {
        isShared = yn;
        return this;
    }

    public ComponentNodeBuilder setIsProvider(boolean yn) {
        isProvider = yn;
        return this;
    }

    public ComponentNodeBuilder setIsProvided(boolean yn) {
        isProvided = yn;
        return this;
    }

    private String fillColor() {
        if (shareable) {
            if (isShared) {
                return SHAREABLE_BGCOLOR;
            } else {
                return SHAREABLE_UNSHARED_BGCOLOR;
            }
        } else {
            return UNSHARED_BGCOLOR;
        }
    }

    @Override
    public GVNode build() {
        NodeBuilder nb = NodeBuilder.create(nodeId);
        if (dependencies.isEmpty() && parameters.isEmpty()) {
            nb.setLabel(label)
              .setShape("box")
              .set("fillcolor", fillColor())
              .set("fontcolor", TYPE_COLOR)
              .set("color", UNSHARED_BGCOLOR)
              .add("style", "filled");
        } else {
            StringBuilder lbl = new StringBuilder();
            lbl.append("<TABLE")
               .append(" CELLSPACING=\"0\"")
               .append(" BORDER=\"1\"")
               .append(" STYLE=\"ROUNDED\"")
               .append(" ROWS=\"*\"")
               .append(" COLOR=\"").append(UNSHARED_BGCOLOR).append("\"")
               .append(">");

            lbl.append("<TR><TD PORT=\"H\" ALIGN=\"CENTER\" BORDER=\"2\"")
               .append(" BGCOLOR=\"")
               .append(fillColor())
               .append("\"");
            lbl.append(">")
               .append("<FONT COLOR=\"")
               .append(TYPE_COLOR)
               .append("\">")
               .append("<B>")
               .append(escapeHtml4(label))
               .append("</B>")
               .append("</FONT>")
               .append("</TD></TR>");

            int i = 1;
            for (String dep: dependencies) {
                lbl.append("<TR><TD BORDER=\"1\" PORT=\"")
                   .append(i++)
                   .append("\" ALIGN=\"LEFT\">")
                   .append(escapeHtml4(dep))
                   .append("</TD></TR>");
            }

            for (String param: parameters) {
                lbl.append("<TR><TD ALIGN=\"LEFT\"");
                lbl.append(">")
                   .append(escapeHtml4(param))
                   .append("</TD></TR>");
            }

            lbl.append("</TABLE>");
            nb.setLabel(new HTMLLabel(lbl.toString()))
              .setTarget(nodeId + ":H")
              .setShape("plaintext")
              .set("margin", 0);
        }

        return nb.build();
    }

    static String shortClassName(Class<?> type) {
        if (ClassUtils.isPrimitiveOrWrapper(type)) {
            if (!type.isPrimitive()) {
                type = ClassUtils.wrapperToPrimitive(type);
            }
            return type.getName();
        } else if (type.getPackage().equals(Package.getPackage("java.lang"))) {
            return type.getSimpleName();
        } else {
            String[] words = type.getName().split(" ");
            String fullClassName = words[words.length - 1];
            String[] path = fullClassName.split("\\.");
            int i = 0;
            while (!Character.isUpperCase(path[i + 1].charAt(0))) {
                path[i] = path[i].substring(0, 1);
                i++;
            }
            return StringUtils.join(path, ".");
        }
    }

    private static final Pattern ANNOT_PATTERN = Pattern.compile("@[^(]+\\((.*)\\)");

    static String shortAnnotation(Annotation annot) {
        Matcher m = ANNOT_PATTERN.matcher(annot.toString());
        if (m.matches()) {
            StringBuilder bld = new StringBuilder();
            bld.append('@');
            bld.append(shortClassName(annot.annotationType()));
            String values = m.group(1);
            if (!values.isEmpty()) {
                bld.append('(');
                bld.append(values);
                bld.append(')');
            }
            return bld.toString();
        } else {
            throw new RuntimeException("invalid annotation string format");
        }
    }
}
