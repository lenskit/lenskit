/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import com.google.common.base.Predicate;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Qualifiers;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * A Grapht injector that uses a precomputed graph.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StaticInjector implements Injector {
    private final NodeInstantiator instantiator;
    private DAGNode<Component, Dependency> graph;

    /**
     * Create a new static injector.
     *
     * @param g   The object graph.
     */
    public StaticInjector(DAGNode<Component,Dependency> g) {
        graph = g;
        instantiator = NodeInstantiator.create();
    }

    @Override
    public <T> T getInstance(Class<T> type) throws InjectionException {
        Desire d = Desires.create(null, type, true);
        DAGEdge<Component, Dependency> e =
                graph.getOutgoingEdgeWithLabel(Dependency.hasInitialDesire(d));

        if (e != null) {
            return type.cast(instantiator.instantiate(e.getTail()));
        } else {
            DAGNode<Component, Dependency> node = findSatisfyingNode(Qualifiers.matchDefault(), type);
            if (node != null) {
                return type.cast(instantiator.instantiate(node));
            } else {
                return null;
            }
        }
    }

    public <T> T getInstance(Class<? extends Annotation> qual, Class<T> type) throws InjectionException {
        return getInstance(Qualifiers.match(qual), type);
    }

    public <T> T getInstance(QualifierMatcher qmatch, Class<T> type) throws InjectionException {
        DAGNode<Component, Dependency> node = findSatisfyingNode(qmatch, type);
        if (node != null) {
            return type.cast(instantiator.instantiate(node));
        } else {
            return null;
        }
    }

    /**
     * Find a node with a satisfaction for a specified type. Does a breadth-first
     * search to find the closest matching one.
     *
     * @param type The type to look for.
     * @return A node whose satisfaction is compatible with {@code type}.
     * @review Decide how to handle qualifiers and contexts
     */
    @Nullable
    private DAGNode<Component,Dependency> findSatisfyingNode(final QualifierMatcher qmatch, final Class<?> type) {
        Predicate<DAGEdge<Component,Dependency>> pred = new Predicate<DAGEdge<Component, Dependency>>() {
            @Override
            public boolean apply(@Nullable DAGEdge<Component, Dependency> input) {
                return input != null
                       && type.isAssignableFrom(input.getTail()
                                                     .getLabel()
                                                     .getSatisfaction()
                                                     .getErasedType())
                       && qmatch.apply(input.getLabel()
                                            .getInitialDesire()
                                            .getInjectionPoint()
                                            .getQualifier());
            }
        };
        DAGEdge<Component, Dependency> edge = graph.findEdgeBFS(pred);
        if (edge != null) {
            return edge.getTail();
        } else {
            return null;
        }
    }


    @Override
    public <T> T getInstance(Annotation qualifier, Class<T> type) throws InjectionException {
        return getInstance(Qualifiers.match(qualifier), type);
    }
}
