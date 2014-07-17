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
package org.grouplens.lenskit.inject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.AbstractSatisfactionVisitor;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.internal.*;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper utilities for Grapht integration.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
public final class GraphtUtils {
    private GraphtUtils() {
    }

//    public static Node replaceNodeWithPlaceholder(InjectSPI spi, Graph graph, Node node) {
//        // replace it with a null satisfaction
//        final Component daoLbl = node.getLabel();
//        assert daoLbl != null;
//        final Satisfaction oldSat = daoLbl.getSatisfaction();
//        final Class<?> type = oldSat.getErasedType();
//        final Satisfaction sat = spi.satisfyWithNull(type);
//        final Node placeholder = new Node(sat, CachePolicy.MEMOIZE);
//        graph.replaceNode(node, placeholder);
//
//        // replace desires on edges (truncates desire chains to only contain head, dropping refs)
//        for (Edge e: Lists.newArrayList(graph.getIncomingEdges(placeholder))) {
//            Desire d = e.getDesire();
//            List<Desire> lbl = null;
//            if (d != null) {
//                lbl = Collections.singletonList(d);
//            }
//            Edge replacement = new Edge(e.getHead(), e.getTail(), lbl);
//            graph.replaceEdge(e, replacement);
//        }
//
//        return placeholder;
//    }

    /**
     * Check a graph for placeholder satisfactions.
     *
     * @param graph The graph to check.
     * @throws org.grouplens.lenskit.core.RecommenderConfigurationException if the graph has a placeholder satisfaction.
     */
    public static void checkForPlaceholders(DAGNode<Component, Dependency> graph, Logger logger) throws RecommenderConfigurationException {
        Set<DAGNode<Component, Dependency>> placeholders = getPlaceholderNodes(graph);
        Satisfaction sat = null;
        for (DAGNode<Component,Dependency> node: placeholders) {
            Component csat = node.getLabel();
            if (sat == null) {
                sat = csat.getSatisfaction();
            }
            logger.error("placeholder {} not removed", csat.getSatisfaction());
        }
        if (sat != null) {
            throw new RecommenderConfigurationException("placeholder " + sat + " not removed");
        }
    }

    /**
     * Get the placeholder nodes from a graph.
     *
     * @param graph The graph.
     * @return The set of nodes that have placeholder satisfactions.
     */
    public static Set<DAGNode<Component, Dependency>> getPlaceholderNodes(DAGNode<Component,Dependency> graph) {
        Predicate<Component> isPlaceholder = new Predicate<Component>() {
            @Override
            public boolean apply(@Nullable Component input) {
                return input != null && input.getSatisfaction() instanceof PlaceholderSatisfaction;
            }
        };
        return FluentIterable.from(graph.getReachableNodes())
                             .filter(DAGNode.labelMatches(isPlaceholder))
                             .toSet();
    }

    /**
     * Determine if a node is a shareable component.
     *
     *
     * @param node The node.
     * @return {@code true} if the component is shareable.
     */
    public static boolean isShareable(DAGNode<Component, Dependency> node) {
        Component label = node.getLabel();
        if (label == null) {
            return false;
        }

        if (label.getSatisfaction().hasInstance()) {
            return true;
        }

        if (label.getCachePolicy() == CachePolicy.NEW_INSTANCE) {
            return false;
        }

        Class<?> type = label.getSatisfaction().getErasedType();
        if (type.getAnnotation(Shareable.class) != null) {
            return true;
        }

        if (type.getAnnotation(Singleton.class) != null) {
            return true;
        }

        // finally examine the satisfaction in more detail
        return label.getSatisfaction().visit(new AbstractSatisfactionVisitor<Boolean>() {
            @Override
            public Boolean visitDefault() {
                return false;
            }

            @Override
            public Boolean visitProviderClass(Class<? extends Provider<?>> pclass) {
                Method m = null;
                try {
                    m = pclass.getMethod("get");
                } catch (NoSuchMethodException e) {
                /* fine, leave it null */
                }
                if (m != null && m.getAnnotation(Shareable.class) != null) {
                    return true;
                }
                return false;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Boolean visitProviderInstance(Provider<?> provider) {
                // cast to raw type to work around inference issue
                return visitProviderClass((Class) provider.getClass());
            }
        });
    }

    /**
     * Determine whether a desire is transient.
     *
     * @param d The desire to test.
     * @return {@code true} if the desire is transient.
     */
    public static boolean desireIsTransient(@Nonnull Desire d) {
        InjectionPoint ip = d.getInjectionPoint();
        return ip.getAttribute(Transient.class) != null;
    }

    public static boolean edgeIsTransient(DAGEdge<?, Dependency> input) {
        Desire desire = input.getLabel().getInitialDesire();
        return desireIsTransient(desire);
    }

    public static Predicate<DAGEdge<?, Dependency>> edgeIsTransient() {
        return new Predicate<DAGEdge<?, Dependency>>() {
            @Override
            public boolean apply(@Nullable DAGEdge<?, Dependency> input) {
                Desire desire = input == null ? null : input.getLabel().getInitialDesire();
                return desire != null && !desireIsTransient(desire);
            }
        };
    }

    private static Function<DAGEdge<Component,Dependency>,List<String>> ORDER_KEY = new Function<DAGEdge<Component, Dependency>, List<String>>() {
        @Nullable
        @Override
        public List<String> apply(@Nullable DAGEdge<Component,Dependency> input) {
            if (input == null) {
                throw new NullPointerException("cannot order null edge");
            }
            Desire desire = input.getLabel().getInitialDesire();
            InjectionPoint ip = desire.getInjectionPoint();
            List<String> key = new ArrayList<String>(4);
            if (ip instanceof ConstructorParameterInjectionPoint) {
                ConstructorParameterInjectionPoint cpi = (ConstructorParameterInjectionPoint) ip;
                key.add("0: constructor");
                key.add(Integer.toString(cpi.getParameterIndex()));
            } else if (ip instanceof SetterInjectionPoint) {
                SetterInjectionPoint spi = (SetterInjectionPoint) ip;
                key.add("1: setter");
                key.add(spi.getMember().getName());
                key.add(Integer.toString(spi.getParameterIndex()));
            } else if (ip instanceof FieldInjectionPoint) {
                FieldInjectionPoint fpi = (FieldInjectionPoint) ip;
                key.add("2: field");
                key.add(fpi.getMember().getName());
            } else if (ip instanceof NoArgumentInjectionPoint) {
                /* this shouldn't really happen */
                NoArgumentInjectionPoint fpi = (NoArgumentInjectionPoint) ip;
                key.add("8: no-arg");
                key.add(fpi.getMember().getName());
            } else if (ip instanceof SimpleInjectionPoint) {
                key.add("5: simple");
            } else {
                key.add("9: unknown");
                key.add(ip.getClass().getName());
            }
            return key;
        }
    };

    /**
     * An ordering over dependency edges.
     */
    public static Ordering<DAGEdge<Component, Dependency>> DEP_EDGE_ORDER =
            Ordering.<String>natural()
                    .lexicographical()
                    .onResultOf(ORDER_KEY);

    /**
     * Find the set of shareable nodes (objects that will be replaced with instance satisfactions in
     * the final graph).
     *
     * @param graph The graph to analyze.
     * @return The set of root nodes - nodes that need to be instantiated and removed. These nodes
     *         are in topologically sorted order.
     */
    public static LinkedHashSet<DAGNode<Component, Dependency>> getShareableNodes(DAGNode<Component, Dependency> graph) {
        LinkedHashSet<DAGNode<Component, Dependency>> shared = Sets.newLinkedHashSet();

        List<DAGNode<Component, Dependency>> nodes = graph.getSortedNodes();
        for (DAGNode<Component, Dependency> node : nodes) {
            if (!isShareable(node)) {
                continue;
            }

            // see if we depend on any non-shared nodes
            // since nodes are sorted, all shared nodes will have been seen
            boolean isShared = true;
            for (DAGEdge<Component,Dependency> edge: node.getOutgoingEdges()) {
                if (!edgeIsTransient(edge)) {
                    isShared &= shared.contains(edge.getTail());
                }
            }
            if (isShared) {
                shared.add(node);
            }
        }

        return shared;
    }
}
