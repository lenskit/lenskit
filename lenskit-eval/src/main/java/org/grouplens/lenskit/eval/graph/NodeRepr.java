package org.grouplens.lenskit.eval.graph;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.InstanceSatisfaction;
import org.grouplens.grapht.spi.reflect.NullSatisfaction;
import org.grouplens.grapht.spi.reflect.ProviderClassSatisfaction;
import org.grouplens.grapht.spi.reflect.ProviderInstanceSatisfaction;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

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

    public String getLabel() {
        Satisfaction sat = node.getLabel().getSatisfaction();
        if (sat instanceof InstanceSatisfaction) {
            return ((InstanceSatisfaction) sat).getInstance().toString();
        } else if (sat instanceof NullSatisfaction) {
            return "null";
        } else if (sat instanceof ProviderInstanceSatisfaction) {
            return "provider " + ((ProviderInstanceSatisfaction) sat).getProvider().toString();
        } else if (sat instanceof ProviderClassSatisfaction) {
            Type type = ((ProviderClassSatisfaction) sat).getProviderType();
            return renderComponent(type, false);
        } else {
            Type type = sat.getErasedType();
            return renderComponent(type, true);
        }
    }

    private String renderComponent(Type type, boolean provider) {
        return Templates.labelTemplate.execute(new Description(type, provider));
    }

    private static String shortenClassName(String name) {
        String[] words = name.split(" ");
        String fullClassName = words[words.length - 1];
        String[] path = fullClassName.split("\\.");
        int i = 0;
        while (!Character.isUpperCase(path[i + 1].charAt(0))) {
            path[i] = path[i].substring(0, 1);
            i++;
        }
        return StringUtils.join(path, ".");
    }

    private class Description {
        private final Type type;
        private final boolean provider;

        public Description(Type t, boolean isP) {
            type = t;
            provider = isP;
        }

        public String getType() {
            return shortenClassName(type.toString());
        }

        public List<String> getDesires() {
            // TODO Inline parameter nodes
            return Lists.transform(
                    edges,
                    new Function<Edge, String>() {
                        @Nullable
                        @Override
                        public String apply(@Nullable Edge e) {
                            Type typ = e.getDesire().getDesiredType();
                            Annotation qual = e.getDesire()
                                               .getInjectionPoint()
                                               .getAttributes()
                                               .getQualifier();
                            if (qual == null) {
                                return typ.toString();
                            } else {
                                return String.format("%s: %s", qual, typ);
                            }
                        }
                    });
        }
    }
}
