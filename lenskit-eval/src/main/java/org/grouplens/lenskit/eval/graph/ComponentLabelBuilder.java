package org.grouplens.lenskit.eval.graph;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.grapht.spi.Desire;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

/**
 * Build a component label.
 */
class ComponentLabelBuilder implements Builder<HTMLLabel> {
    private final String label;
    private final List<String> dependencies = new ArrayList<String>();

    /**
     * Create a new component label builder.
     * @param type The type of component to label.
     */
    public ComponentLabelBuilder(Class<?> type) {
        label = shortClassName(type);
    }

    /**
     * Add a dependency of this component.
     * @param dep The dependency.
     * @return The builder (for chaining).
     */
    public ComponentLabelBuilder addDependency(Desire dep) {
        Annotation q = dep.getInjectionPoint().getAttributes().getQualifier();
        Class<?> type = dep.getDesiredType();
        if (q == null) {
            dependencies.add(shortClassName(type));
        } else {
            dependencies.add(shortAnnotation(q) + ": " + shortClassName(type));
        }
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

    @Override
    public HTMLLabel build() {
        // FIXME Render provider types differently
        StringBuilder lbl = new StringBuilder();
        lbl.append("<FONT FACE=\"sans-serif\"><TABLE CELLSPACING=\"0\">");

        lbl.append("<TR><TD PORT=\"0\" ALIGN=\"CENTER\" CELLPADDING=\"4\">");
        lbl.append(escapeHtml4(label));
        lbl.append("</TD></TR>");

        int i = 1;
        for (String dep: dependencies) {
            lbl.append("<TR><TD PORT=\"")
               .append(i++)
               .append("\" ALIGN=\"LEFT\">");
            lbl.append(escapeHtml4(dep));
            lbl.append("</TD></TR>");
        }

        lbl.append("</TABLE></FONT>");

        return new HTMLLabel(lbl.toString());
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
