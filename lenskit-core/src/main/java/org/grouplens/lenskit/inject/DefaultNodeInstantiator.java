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
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DefaultNodeInstantiator extends NodeInstantiator {
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
