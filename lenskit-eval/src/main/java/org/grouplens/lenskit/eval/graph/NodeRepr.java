package org.grouplens.lenskit.eval.graph;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.InstanceSatisfaction;
import org.grouplens.grapht.spi.reflect.NullSatisfaction;
import org.grouplens.grapht.spi.reflect.ProviderClassSatisfaction;
import org.grouplens.grapht.spi.reflect.ProviderInstanceSatisfaction;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Node representation for graph rendering.
*/
class NodeRepr {
    private final String id;
    private final Node node;
    private final List<Edge> edges;

    public NodeRepr(int i, Node nd, Iterable<Edge> es) {
        id = "C" + i;
        node = nd;
        edges = Lists.newArrayList(es);
    }

    public int edgePort(Edge e) {
        if (complete()) {
            return -1;
        } else if (e == null) {
            return 0;
        } else {
            int c = edges.indexOf(e);
            if (c >= 0) {
                return c + 1;
            } else {
                throw new IllegalArgumentException("nonexistent edge");
            }
        }
    }

    public String getId() {
        return id;
    }

    public boolean complete() {
        Satisfaction sat = node.getLabel().getSatisfaction();
        return sat.hasInstance() || sat instanceof ProviderInstanceSatisfaction;
    }

    public boolean component() {
        return !complete();
    }

    public String getLabel() {
        Satisfaction sat = node.getLabel().getSatisfaction();
        if (DataAccessObject.class.isAssignableFrom(sat.getErasedType())) {
            return "DAO";
        } else if (sat instanceof InstanceSatisfaction) {
            return ((InstanceSatisfaction) sat).getInstance().toString();
        } else if (sat instanceof NullSatisfaction) {
            return "null";
        } else if (sat instanceof ProviderInstanceSatisfaction) {
            return "provider " + ((ProviderInstanceSatisfaction) sat).getProvider().toString();
        } else if (sat instanceof ProviderClassSatisfaction) {
            Class<?> type = ((ProviderClassSatisfaction) sat).getProviderType();
            return renderComponent(type, false);
        } else {
            Class<?> type = sat.getErasedType();
            return renderComponent(type, true);
        }
    }

    public String getType() {
        Satisfaction sat = node.getLabel().getSatisfaction();
        return shortClassName(sat.getErasedType());
    }

    private String renderComponent(Class<?> type, boolean provider) {
        return Templates.labelTemplate.execute(new Description(type, provider));
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

    private class Description {
        private final Class<?> type;
        private final boolean provider;

        public Description(Class<?> t, boolean isP) {
            type = t;
            provider = isP;
        }

        public String getType() {
            return shortClassName(type);
        }

        public List<String> getDesires() {
            // TODO Inline parameter nodes
            return Lists.transform(
                    edges,
                    new Function<Edge, String>() {
                        @Nullable
                        @Override
                        public String apply(@Nullable Edge e) {
                            Class<?> typ = e.getDesire().getDesiredType();
                            Annotation qual = e.getDesire()
                                               .getInjectionPoint()
                                               .getAttributes()
                                               .getQualifier();
                            if (qual == null) {
                                return shortClassName(typ);
                            } else {
                                return String.format("%s: %s",
                                                     shortAnnotation(qual),
                                                     shortClassName(typ));
                            }
                        }
                    });
        }
    }
}
