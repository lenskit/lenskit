package org.grouplens.lenskit.core;

import org.grouplens.grapht.Injector;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.ProviderSource;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * A Grapht injector that uses a precomputed graph.
 * @author Michael Ekstrand
 */
public class StaticInjector implements Injector {
    private InjectSPI spi;
    private Graph graph;
    private Node root;
    private Map<Node, Object> cache;

    /**
     * Create a new static injector. The node labelled with
     * {@code null} is the root node.
     * @param spi The inject SPI.
     * @param g The object graph.
     */
    public StaticInjector(InjectSPI spi, Graph g) {
        this(spi, g, g.getNode(null));
    }
    /**
     * Create a new static injector with a specified root node.
     * @param spi The inject SPI.
     * @param g The object graph.
     * @param rt The root node.
     */
    public StaticInjector(InjectSPI spi, Graph g, Node rt) {
        this.spi = spi;
        graph = g;
        root = rt;
        cache = new HashMap<Node, Object>();
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        Desire d = spi.desire(null, type, false);
        Edge e = graph.getOutgoingEdge(root, d);

        if (e != null) {
            return type.cast(instantiate(e.getTail()));
        } else {
            // FIXME support retrieving deep nodes
            return null;
        }
    }

    /**
     * Instantiate a particular node in the graph.
     * @param node The node to instantiate.
     * @return The instantiation of the node.
     */
    public Object instantiate(Node node) {
        // FIXME Respect cache policies
        // FIXME Use memoizing providers
        Object instance = cache.get(node);
        if (instance != null) {
            CachedSatisfaction lbl = node.getLabel();
            assert lbl != null;
            Provider<?> provider = lbl.getSatisfaction().makeProvider(new DepSrc(node));
            instance = provider.get();
            cache.put(node, lbl);
        }
        return instance;
    }

    @Override
    public <T> T getInstance(Annotation qualifier, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    private class DepSrc implements ProviderSource {
        private Node node;

        private DepSrc(Node n) {
            this.node = n;
        }

        @Override
        public Provider<?> apply(Desire desire) {
            final Node dep = graph.getOutgoingEdge(node, desire).getTail();
            return new Provider() {
                @Override
                public Object get() {
                    return instantiate(dep);
                }
            };
        }
    }
}
