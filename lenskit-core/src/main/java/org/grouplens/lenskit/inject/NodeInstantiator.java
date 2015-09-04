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
import com.google.common.base.Preconditions;
import org.grouplens.grapht.*;
import org.grouplens.grapht.graph.DAGNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Instantiate graph nodes.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class NodeInstantiator implements Function<DAGNode<Component,Dependency>,Object> {
    public static NodeInstantiator create() {
        return new DefaultImpl();
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
            throw new RuntimeException("Instantiation error on " + input.getLabel(), e);
        }
    }

    /**
     * Default implementation of the {@link org.grouplens.lenskit.inject.NodeInstantiator} interface.
     *
     * @since 2.1
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    static class DefaultImpl extends NodeInstantiator {
        private final InjectionContainer container;

        DefaultImpl() {
            container = InjectionContainer.create(CachePolicy.MEMOIZE);
        }

        @Override
        public Object instantiate(DAGNode<Component, Dependency> node) throws InjectionException {
            Instantiator inst;
            synchronized (container) {
                inst = container.makeInstantiator(node);
            }
            return inst.instantiate();
        }
    }
}
