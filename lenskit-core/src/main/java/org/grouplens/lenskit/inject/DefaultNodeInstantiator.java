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

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.ProviderSource;
import org.grouplens.grapht.util.MemoizingProvider;

import javax.inject.Provider;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Default implementation of the {@link NodeInstantiator} interface.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class DefaultNodeInstantiator extends NodeInstantiator {
    private Map<DAGNode<Component,Dependency>, Provider<?>> providerCache;

    DefaultNodeInstantiator() {
        providerCache = new WeakHashMap<DAGNode<Component, Dependency>, Provider<?>>();
    }

    @Override
    public Object instantiate(DAGNode<Component, Dependency> node) {
        Provider<?> p = getProvider(node);

        return p.get();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private synchronized Provider<?> getProvider(DAGNode<Component, Dependency> node) {
        Provider<?> provider = providerCache.get(node);
        if (provider == null) {
            Component lbl = node.getLabel();
            assert lbl != null;
            provider = lbl.getSatisfaction().makeProvider(new DepSrc(node));
            CachePolicy pol = lbl.getCachePolicy();
            if (pol == CachePolicy.NO_PREFERENCE) {
                pol = lbl.getSatisfaction().getDefaultCachePolicy();
            }
            switch (pol) {
            case NEW_INSTANCE:
                break;
            default:
                // TODO allow default policy to be specified
                provider = new MemoizingProvider(provider);
                break;
            }
            providerCache.put(node, provider);
        }
        return provider;
    }

    private class DepSrc implements ProviderSource {
        private DAGNode<Component, Dependency> node;

        private DepSrc(DAGNode<Component, Dependency> n) {
            this.node = n;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Provider<?> apply(Desire desire) {
            final DAGNode<Component, Dependency> dep =
                    node.getOutgoingEdgeWithLabel(Dependency.hasInitialDesire(desire)).getTail();
            return new Provider() {
                @Override
                public Object get() {
                    return instantiate(dep);
                }
            };
        }
    }
}
