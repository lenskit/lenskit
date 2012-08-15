/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.core;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.grouplens.grapht.*;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.*;
import org.grouplens.lenskit.*;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.*;

import static org.grouplens.grapht.BindingFunctionBuilder.RuleSet;

/**
 * {@link RecommenderEngineFactory} that builds a LenskitRecommenderEngine.
 * <p>
 *     This class is final for copying safety. This decision can be revisited.
 * </p>
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @compat Public
 */
public final class LenskitRecommenderEngineFactory implements RecommenderEngineFactory, Context {
    private static final Class<?>[] INITIAL_ROOTS = {
            RatingPredictor.class,
            ItemScorer.class,
            GlobalItemScorer.class,
            ItemRecommender.class,
            GlobalItemRecommender.class
    };

    private final BindingFunctionBuilder config;
    private final DAOFactory factory;
    private final Set<Class<?>> roots;

    public LenskitRecommenderEngineFactory() {
        this((DAOFactory) null);
    }

    public LenskitRecommenderEngineFactory(@Nullable DAOFactory factory) {
        this.factory = factory;
        config = new BindingFunctionBuilder();
        roots = new HashSet<Class<?>>();
        Collections.addAll(roots, INITIAL_ROOTS);
    }

    private LenskitRecommenderEngineFactory(LenskitRecommenderEngineFactory engineFactory) {
        factory = engineFactory.factory;
        config = engineFactory.config.clone();
        roots = new HashSet<Class<?>>(engineFactory.roots);
    }

    /**
     * Add the specified component type as a root component. This forces it (and its
     * dependencies) to be resolved, and makes it available from the resulting
     * recommenders.
     *
     * @param componentType The type of component to add as a root (typically an interface).
     * @see LenskitRecommender#get(Class)
     */
    public void addRoot(Class<?> componentType) {
        roots.add(componentType);
    }

    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return config.getRootContext().bind(type);
    }

    @Override
    public void bind(Class<? extends Annotation> param, Object value) {
        config.getRootContext().bind(param, value);
    }

    public <T> Binding<T> bind(Class<? extends Annotation> qualifier, Class<T> type) {
        return bind(type).withQualifier(qualifier);
    }

    @Override
    public Context in(Class<?> type) {
        return config.getRootContext().in(type);
    }

    @Override
    public Context in(Class<? extends Annotation> qualifier, Class<?> type) {
        return config.getRootContext().in(qualifier, type);
    }

    @Override
    public Context in(Annotation qualifier, Class<?> type) {
        return config.getRootContext().in(qualifier, type);
    }

    public Context in(String name, Class<?> type) {
        // REVIEW: Do we want to keep this method? Do we want to add it to Grapht?
        return config.getRootContext().in(Names.named(name), type);
    }

    /**
     * Groovy-compatible alias for {@link #in(Class)}.
     */
    @SuppressWarnings("unused")
    public Context within(Class<?> type) {
        return in(type);
    }

    /**
     * Groovy-compatible alias for {@link #in(Class, Class)}.
     */

    public Context within(Class<? extends Annotation> qualifier, Class<?> type) {
        return in(qualifier, type);
    }

    /**
     * Groovy-compatible alias for {@link #in(String, Class)}.
     */
    public Context within(String name, Class<?> type) {
        return in(name, type);
    }

    @Override
    public LenskitRecommenderEngineFactory clone() {
        return new LenskitRecommenderEngineFactory(this);
    }

    @Override
    public LenskitRecommenderEngine create() {
        if (factory == null) {
            throw new IllegalStateException("create() called with no DAOFactory");
        }
        DataAccessObject dao = factory.snapshot();
        try {
            return create(dao);
        } finally {
            dao.close();
        }
    }

    private void resolve(Class<?> type, DependencySolver solver) {
        try {
            solver.resolve(config.getSPI().desire(null, type, true));
        } catch (SolverException e) {
            throw new InjectionException(type, null, e);
        }
    }

    public LenskitRecommenderEngine create(DataAccessObject dao) {
        BindingFunctionBuilder config = this.config.clone();
        config.getRootContext().bind(DataAccessObject.class).to(dao);

        DependencySolver solver = new DependencySolver(
                Arrays.asList(config.build(RuleSet.EXPLICIT),
                              config.build(RuleSet.INTERMEDIATE_TYPES),
                              config.build(RuleSet.SUPER_TYPES),
                              new DefaultDesireBindingFunction(config.getSPI())),
                100);

        // Resolve all required types to complete a Recommender
        for (Class<?> root : roots) {
            resolve(root, solver);
        }

        // At this point the graph contains the dependency state to build a
        // recommender with the current DAO. Any extra bind rules don't matter
        // because they could not have created any Nodes.
        Graph original = solver.getGraph();

        // Get the set of shareable instances.
        Set<Node> shared = getShareableNodes(original);

        // Instantiate and replace shareable nodes
        Graph modified = original.clone();
        Set<Node> sharedInstances = instantiate(modified, shared);

        // Remove transient edges and orphaned subgraphs
        Set<Node> transientTargets = removeTransientEdges(modified, sharedInstances);
        removeOrphanSubgraphs(modified, transientTargets);

        // Find the DAO node
        Node daoNode = GraphtUtils.findDAONode(modified);
        Node daoPlaceholder = null;
        if (daoNode != null) {
            // replace it with a null satisfaction
            CachedSatisfaction daoLbl = daoNode.getLabel();
            assert daoLbl != null;
            Class<?> type = daoLbl.getSatisfaction().getErasedType();
            Satisfaction sat = config.getSPI().satisfyWithNull(type);
            daoPlaceholder = new Node(sat, CachePolicy.MEMOIZE);
            modified.replaceNode(daoNode, daoPlaceholder);
        }

        return new LenskitRecommenderEngine(factory, modified, daoPlaceholder,
                                            config.getSPI());
    }

    /**
     * Prune the graph, returning the set of nodes for shareable objects
     * (objects that will be replaced with instance satisfactions in the
     * final graph).
     *
     * @param graph The graph to analyze. The graph is not modified.
     * @return The set of root nodes - nodes that need to be instantiated and
     *         removed. These nodes are in topologically sorted order.
     */
    private LinkedHashSet<Node> getShareableNodes(Graph graph) {
        LinkedHashSet<Node> shared = new LinkedHashSet<Node>();

        List<Node> nodes = graph.sort(graph.getNode(null));
        for (Node node : nodes) {
            if (!GraphtUtils.isShareable(node)) {
                continue;
            }

            // see if we depend on any non-shared nodes
            // since nodes are sorted, all shared nodes will have been seen
            Set<Edge> intransient = GraphtUtils.removeTransient(graph.getOutgoingEdges(node));
            boolean isShared =
                    Iterables.all(Iterables.transform(intransient, GraphtUtils.edgeTail()),
                                  Predicates.in(shared));
            if (isShared) {
                shared.add(node);
            }
        }

        return shared;
    }

    /**
     * Instantiate the shared objects in a graph. This instantiates all shared objects,
     * and replaces their nodes with nodes wrapping instance satisfactions.
     *
     * @param graph     The complete configuration graph. This graph will be modified.
     * @param toReplace The shared nodes to replace.
     * @return The new instance nodes, in iteration order from {@code toReplace}.
     */
    private LinkedHashSet<Node> instantiate(Graph graph, Set<Node> toReplace) {
        InjectSPI spi = config.getSPI();
        StaticInjector injector = new StaticInjector(spi, graph);
        LinkedHashSet<Node> replacements = new LinkedHashSet<Node>();
        for (Node node : toReplace) {
            Object obj = injector.instantiate(node);
            CachedSatisfaction label = node.getLabel();
            assert label != null;
            Satisfaction instanceSat;
            if (obj == null) {
                instanceSat = spi.satisfyWithNull(label.getSatisfaction().getErasedType());
            } else {
                instanceSat = spi.satisfy(obj);
            }
            Node repl = new Node(instanceSat, label.getCachePolicy());
            graph.replaceNode(node, repl);
        }
        return replacements;
    }

    /**
     * Remove transient edges from a graph.
     *
     * @param graph The graph to remove transient edges from.
     * @param nodes The nodes whose outgoing transient edges should be removed.
     * @return The set of tail nodes of removed edges.
     */
    private Set<Node> removeTransientEdges(Graph graph, Set<Node> nodes) {
        Set<Node> targets = new HashSet<Node>();
        Set<Node> seen = new HashSet<Node>();
        Queue<Node> work = new LinkedList<Node>();
        work.addAll(nodes);
        seen.addAll(nodes);
        while (!work.isEmpty()) {
            Node node = work.remove();
            for (Edge e : graph.getOutgoingEdges(node)) {
                Node nbr = e.getTail();

                // remove transient edges, traverse non-transient ones
                Desire desire = e.getDesire();
                assert desire != null;
                if (GraphtUtils.desireIsTransient(desire)) {
                    graph.removeEdge(e);
                    targets.add(nbr);
                } else if (!seen.contains(nbr)) {
                    seen.add(nbr);
                    work.add(nbr);
                }
            }
        }
        return targets;
    }

    private void removeOrphanSubgraphs(Graph graph, Collection<Node> candidates) {
        Queue<Node> removeQueue = new LinkedList<Node>(candidates);
        while (!removeQueue.isEmpty()) {
            Node candidate = removeQueue.poll();
            Set<Edge> incoming = graph.getIncomingEdges(candidate); // null if candidate got re-added
            if (incoming != null && incoming.isEmpty()) {
                // No other node depends on this node, so we can remove it,
                // we must also flag its dependencies as removal candidates
                for (Edge e : graph.getOutgoingEdges(candidate)) {
                    removeQueue.add(e.getTail());
                }
                graph.removeNode(candidate);
            }
        }
    }
}
