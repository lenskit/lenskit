/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.inject;

import org.grouplens.grapht.*;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Qualifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * A Grapht injector that uses a precomputed graph.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StaticInjector implements Injector {
    private static final Logger logger = LoggerFactory.getLogger(StaticInjector.class);

    private final LifecycleManager lifecycle;
    private RuntimeException capture;
    private boolean closed = false;
    private final NodeInstantiator instantiator;
    private DAGNode<Component, Dependency> graph;

    /**
     * Create a new static injector.
     *
     * @param g   The object graph.
     */
    public StaticInjector(DAGNode<Component,Dependency> g) {
        graph = g;
        lifecycle = new LifecycleManager();
        instantiator = NodeInstantiator.create(lifecycle);
        capture = new RuntimeException("Static injector instantiated (backtrace shows instantiation point)");
    }

    @Override
    public <T> T getInstance(Class<T> type) throws InjectionException {
        T obj = tryGetInstance(Qualifiers.matchDefault(), type);
        if (obj == null) {
            throw new ResolutionException("no resolution available for " + type);
        } else {
            return obj;
        }
    }

    public <T> T tryGetInstance(Class<? extends Annotation> qual, Class<T> type) throws InjectionException {
        return tryGetInstance(Qualifiers.match(qual), type);
    }

    public <T> T tryGetInstance(QualifierMatcher qmatch, Class<T> type) throws InjectionException {
        DAGNode<Component, Dependency> node = GraphtUtils.findSatisfyingNode(graph, qmatch, type);
        return node != null ? type.cast(instantiator.instantiate(node)) : null;
    }

    @Nullable
    public <T> T tryGetInstance(Class<T> type) throws InjectionException {
        Desire d = Desires.create(null, type, true);
        DAGEdge<Component, Dependency> e =
                graph.getOutgoingEdgeWithLabel(l -> l.hasInitialDesire(d));

        if (e != null) {
            return type.cast(instantiator.instantiate(e.getTail()));
        } else {
            DAGNode<Component, Dependency> node = GraphtUtils.findSatisfyingNode(graph, Qualifiers.matchDefault(), type);
            if (node != null) {
                return type.cast(instantiator.instantiate(node));
            } else {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public <T> T tryGetInstance(Annotation qualifier, Class<T> type) throws InjectionException {
        return tryGetInstance(Qualifiers.match(qualifier), type);
    }

    @Override
    public <T> T getInstance(Annotation qualifier, Class<T> type) throws InjectionException {
        T obj = tryGetInstance(Qualifiers.match(qualifier), type);
        if (obj == null) {
            throw new ResolutionException("no resolution available for " + type + " with qualifier " + qualifier);
        } else {
            return obj;
        }
    }

    @Override
    public void close() {
        lifecycle.close();
        closed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!closed) {
            logger.warn("Injector {} was never closed", this, capture);
        }
        super.finalize();
    }
}
