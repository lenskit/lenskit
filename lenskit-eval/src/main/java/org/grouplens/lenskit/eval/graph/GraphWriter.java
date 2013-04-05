package org.grouplens.lenskit.eval.graph;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.grouplens.grapht.spi.Desire;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Write a graph in GraphViz format.
 */
public class GraphWriter implements Closeable {
    private final PrintWriter output;

    public GraphWriter(Writer out) {
        output = new PrintWriter(out);
        output.println("digraph {");
    }

    @Override
    public void close() throws IOException {
        output.println("}");
        output.close();
    }

    /**
     * Put the root node.
     *
     * @param id The root node ID.
     */
    public void putRootNode(String id) {
        output.format("  %s [label=Root,shape=diamond];\n", id);
    }

    /**
     * Put a null node.
     *
     * @param id   The node ID.
     * @param type The type of null.
     */
    public void putNullNode(String id, Class<?> type) {
        output.format("  %s [label=null,shape=ellipse];\n", id);
    }

    /**
     * Write an object node.
     *
     * @param id The node ID.
     * @param obj The object.
     * @param isProvider Is this object a provider?
     */
    public void putObjectNode(String id, Object obj, boolean isProvider) {
        // FIXME Render provider objects differently
        String label = StringEscapeUtils.escapeJava(obj.toString());
        output.format("  %s [label=\"%s\",shape=ellipse];\n", id, label);
    }

    /**
     * Write a simple type node to the output.
     * @param id The node ID.
     * @param type The component type in the node.
     */
    public void putTypeNode(String id, Type type) {
        String label = type.toString();
        output.format("  %s [label=\"%s\",shape=box];\n", id, label);
    }

    /**
     * Write a simple type node to the output.
     * @param id The node ID.
     * @param type The component type in the node.
     */
    public void putComponentNode(String id, Class<?> type, List<Desire> deps, boolean isProvider) {
        // FIXME Render provider types differently
        StringBuilder lbl = new StringBuilder();
        lbl.append("<FONT FACE=\"sans-serif\"><TABLE CELLSPACING=\"0\">");

        lbl.append("<TR><TD PORT=\"0\" ALIGN=\"CENTER\" CELLPADDING=\"4\">");
        lbl.append(StringEscapeUtils.escapeHtml4(shortClassName(type)));
        lbl.append("</TD></TR>");

        int i = 1;
        for (Desire d: deps) {
            lbl.append("<TR><TD PORT=\"")
               .append(i++)
               .append("\" ALIGN=\"LEFT\">");
            Annotation q = d.getInjectionPoint().getAttributes().getQualifier();
            Class<?> dtype = d.getDesiredType();
            if (q == null) {
                lbl.append(StringEscapeUtils.escapeHtml4(shortClassName(dtype)));
            } else {
                lbl.append(StringEscapeUtils.escapeHtml4(shortAnnotation(q)))
                   .append(": ")
                   .append(StringEscapeUtils.escapeHtml4(shortClassName(dtype)));
            }
            lbl.append("</TD></TR>");
        }
        lbl.append("</TABLE></FONT>");

        output.format("  %s [label=<%s>,shape=plaintext];\n", id, lbl.toString());
    }

    /**
     * Write a dependency edge.
     *
     * @param srcId The source node ID.
     * @param tgtId The target node ID.
     */
    public void putEdgeDepends(String srcId, String tgtId) {
        output.format("  %s -> %s [arrowhead=vee];\n", srcId, tgtId);
    }

    /**
     * Write a "provides" edge.
     *
     * @param id The node provided.
     * @param pid The provider node.
     */
    public void putEdgeProvidedBy(String id, String pid) {
        output.format("  %s -> %s [dir=back,arrowhead=onormal,style=dashed];\n", id, pid);
    }

    private static String shortClassName(Class<?> type) {
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

    private static String shortAnnotation(Annotation annot) {
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
