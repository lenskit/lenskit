/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.spi.*;
import org.grouplens.grapht.util.MemoizingProvider;
import org.grouplens.lenskit.core.LenskitConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * A Grapht injector that uses a precomputed graph.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StaticInjector implements Injector, Function<DAGNode<CachedSatisfaction,DesireChain>,Object> {
    private InjectSPI spi;
    private DAGNode<CachedSatisfaction, DesireChain> graph;
    private Map<DAGNode<CachedSatisfaction,DesireChain>, Provider<?>> providerCache;

    /**
     * Create a new static injector.
     *
     * @param g   The object graph.
     */
    public StaticInjector(DAGNode<CachedSatisfaction, DesireChain> g) {
        spi = LenskitConfiguration.LENSKIT_SPI;
        graph = g;
        providerCache = Maps.newHashMap();
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        Desire d = spi.desire(null, type, true);
        DAGEdge<CachedSatisfaction, DesireChain> e =
                graph.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d));

        if (e != null) {
            return type.cast(instantiate(e.getTail()));
        } else {
            DAGNode<CachedSatisfaction,DesireChain> node = findSatisfyingNode(spi.matchDefault(), type);
            if (node != null) {
                return type.cast(instantiate(node));
            } else {
                return null;
            }
        }
    }

    public <T> T getInstance(Class<? extends Annotation> qual, Class<T> type) {
        DAGNode<CachedSatisfaction,DesireChain> node = findSatisfyingNode(spi.match(qual), type);
        if (node != null) {
            return type.cast(instantiate(node));
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
    private DAGNode<CachedSatisfaction,DesireChain> findSatisfyingNode(final QualifierMatcher qmatch, final Class<?> type) {
        Predicate<DAGEdge<CachedSatisfaction,DesireChain>> pred = new Predicate<DAGEdge<CachedSatisfaction, DesireChain>>() {
            @Override
            public boolean apply(@Nullable DAGEdge<CachedSatisfaction, DesireChain> input) {
                return input != null
                       && type.isAssignableFrom(input.getTail()
                                                     .getLabel()
                                                     .getSatisfaction()
                                                     .getErasedType())
                       && qmatch.apply(input.getLabel()
                                            .getInitialDesire()
                                            .getInjectionPoint()
                                            .getAttributes()
                                            .getQualifier());
            }
        };
        DAGEdge<CachedSatisfaction,DesireChain> edge = graph.findEdgeBFS(pred);
        if (edge != null) {
            return edge.getTail();
        } else {
            return null;
        }
    }

    /**
     * Instantiate a particular node in the graph.
     *
     * @param node The node to instantiate.
     * @return The instantiation of the node.
     */
    public Object instantiate(DAGNode<CachedSatisfaction,DesireChain> node) {
        Provider<?> p = getProvider(node);

        return p.get();
    }

    @Nonnull
    @Override
    public Object apply(@Nullable DAGNode<CachedSatisfaction, DesireChain> input) {
        Preconditions.checkNotNull(input, "input node");
        return instantiate(input);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private synchronized Provider<?> getProvider(DAGNode<CachedSatisfaction,DesireChain> node) {
        Provider<?> provider = providerCache.get(node);
        if (provider == null) {
            CachedSatisfaction lbl = node.getLabel();
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

    @Override
    public <T> T getInstance(Annotation qualifier, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    private class DepSrc implements ProviderSource {
        private DAGNode<CachedSatisfaction,DesireChain> node;

        private DepSrc(DAGNode<CachedSatisfaction,DesireChain> n) {
            this.node = n;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Provider<?> apply(Desire desire) {
            final DAGNode<CachedSatisfaction,DesireChain> dep =
                    node.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(desire)).getTail();
            return new Provider() {
                @Override
                public Object get() {
                    return instantiate(dep);
                }
            };
        }
    }
}
