package org.grouplens.lenskit.eval.graph;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Desire;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

/**
 * Build a component label.
 */
class ComponentNodeBuilder implements Builder<Pair<String,Map<String,Object>>> {
    static final String SHAREABLE_COLOR = "#73d216";

    private final String nodeId;
    private final String label;
    private final List<String> dependencies = new ArrayList<String>();
    private final List<String> parameters = new ArrayList<String>();
    private boolean shareable = false;
    private boolean isProvider = false;
    private boolean isProvided = false;

    /**
     * Create a new component label builder.
     * @param type The type of component to label.
     */
    public ComponentNodeBuilder(String id, Class<?> type) {
        nodeId = id;
        label = shortClassName(type);
    }

    /**
     * Add a dependency of this component.
     * @param dep The dependency.
     * @return The builder (for chaining).
     */
    public ComponentNodeBuilder addDependency(Desire dep) {
        Annotation q = dep.getInjectionPoint().getAttributes().getQualifier();
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

    public ComponentNodeBuilder setIsProvider(boolean yn) {
        isProvider = yn;
        return this;
    }

    public ComponentNodeBuilder setIsProvided(boolean yn) {
        isProvided = yn;
        return this;
    }

    @Override
    public Pair<String, Map<String, Object>> build() {
        NodeBuilder nb = new NodeBuilder(nodeId);
        if (dependencies.isEmpty() && parameters.isEmpty()) {
            nb.setLabel(label)
              .setShape("box");
            if (shareable) {
                nb.set("fillcolor", SHAREABLE_COLOR)
                  .add("style", "filled");
            }
            if (isProvided) {
                nb.add("style", "dashed");
            }
        } else {
            StringBuilder lbl = new StringBuilder();
            lbl.append("<TABLE CELLSPACING=\"0\" BORDER=\"0\">");

            lbl.append("<TR><TD PORT=\"0\" ALIGN=\"CENTER\" BORDER=\"2\"");
            if (shareable) {
                lbl.append(" BGCOLOR=\"")
                   .append(SHAREABLE_COLOR)
                   .append("\"");
            }
            lbl.append(">")
               .append(escapeHtml4(label))
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
                lbl.append("<TR><TD BORDER=\"1\" ALIGN=\"LEFT\">")
                   .append(escapeHtml4(param))
                   .append("</TD></TR>");
            }

            lbl.append("</TABLE>");
            nb.setLabel(new HTMLLabel(lbl.toString()))
              .setShape("plaintext");
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
