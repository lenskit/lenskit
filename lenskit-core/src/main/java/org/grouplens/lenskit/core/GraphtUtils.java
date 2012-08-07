package org.grouplens.lenskit.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Desire;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Helper utilities for Grapht integration.
 * @author Michael Ekstrand
 * @since 0.11
 */
class GraphtUtils {
    private GraphtUtils() {}

    /**
     * Remove transient edges from a set.
     * @param edges The set of edges.
     * @return A new set containing only the non-transient edges.
     */
    public static Set<Edge> removeTransient(Set<Edge> edges) {
        return Sets.filter(edges, new Predicate<Edge>() {
            @Override
            public boolean apply(@Nullable Edge input) {
                Desire desire = input == null ? null : input.getDesire();
                return desire != null && desireIsTransient(desire);
            }
        });
    }

    /**
     * Determine whether a desire is transient.
     * @param d The desire to test.
     * @return {@code true} if the desire is transient.
     */
    public static boolean desireIsTransient(@Nonnull Desire d) {
        Attributes attrs = d.getInjectionPoint().getAttributes();
        return attrs.getAttribute(Transient.class) != null;
    }

    /**
     * Function to extract the tail of a node.
     * @return A function extracting the tail of a node.
     */
    public static Function<Edge,Node> edgeTail() {
        return EdgeTail.instance;
    }

    private static class EdgeTail implements Function<Edge,Node> {
        public static EdgeTail instance = new EdgeTail();

        @Override
        public Node apply(@Nullable Edge input) {
            return input == null ? null : input.getTail();
        }
    }
}
