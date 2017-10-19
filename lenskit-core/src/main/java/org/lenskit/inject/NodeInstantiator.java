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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.grouplens.grapht.*;
import org.grouplens.grapht.graph.DAGNode;
import org.lenskit.api.RecommenderBuildException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;

/**
 * Instantiate graph nodes.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class NodeInstantiator implements Function<DAGNode<Component,Dependency>,Object> {
    /**
     * Create a node instantiator without a lifecycle manager.
     * @return A node instantiator that does not support lifecycle management.
     */
    public static NodeInstantiator create() {
        return new DefaultImpl(null);
    }

    /**
     * Create a node instantiator with a lifecycle manager.
     * @param mgr The lifecycle manager to use.
     * @return A node instantiator that will register components with a lifecycle manager.
     */
    public static NodeInstantiator create(@WillNotClose LifecycleManager mgr) {
        return new DefaultImpl(mgr);
    }

    /**
     * Instantiate a particular node in the graph.
     *
     * @param node The node to instantiate.
     * @return The instantiation of the node.
     */
    public abstract Object instantiate(DAGNode<Component, Dependency> node) throws InjectionException;

    @Nonnull
    @Override
    public Object apply(@Nullable DAGNode<Component, Dependency> input) {
        Preconditions.checkNotNull(input, "input node");
        try {
            return instantiate(input);
        } catch (InjectionException e) {
            throw new RecommenderBuildException("cannot instantiate " + input.getLabel(), e);
        }
    }

    /**
     * Default implementation of the {@link org.lenskit.inject.NodeInstantiator} interface.
     *
     * @since 2.1
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    static class DefaultImpl extends NodeInstantiator {
        private final InjectionContainer container;

        DefaultImpl(LifecycleManager mgr) {
            container = InjectionContainer.create(CachePolicy.MEMOIZE, mgr);
        }

        @Override
        public Object instantiate(DAGNode<Component, Dependency> node) throws InjectionException {
            return container.makeInstantiator(node).instantiate();
        }
    }
}
