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

import static org.grouplens.grapht.BindingFunctionBuilder.RuleSet;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.grouplens.grapht.*;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.*;
import org.grouplens.grapht.util.InstanceProvider;
import org.grouplens.lenskit.*;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.Map.Entry;

/**
 * {@link RecommenderEngineFactory} that builds a LenskitRecommenderEngine.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class LenskitRecommenderEngineFactory implements RecommenderEngineFactory, Cloneable, Context {
    private final BindingFunctionBuilder config;
    private final DAOFactory factory;

    public LenskitRecommenderEngineFactory() {
        this((DAOFactory) null);
    }

    public LenskitRecommenderEngineFactory(@Nullable DAOFactory factory) {
        this.factory = factory;
        config = new BindingFunctionBuilder();
    }

    private LenskitRecommenderEngineFactory(LenskitRecommenderEngineFactory engineFactory) {
        factory = engineFactory.factory;
        config = engineFactory.config.clone();
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
        resolve(RatingPredictor.class, solver);
        resolve(ItemScorer.class, solver);
        resolve(GlobalItemScorer.class, solver);
        resolve(ItemRecommender.class, solver);
        resolve(GlobalItemRecommender.class, solver);

        // At this point the graph contains the dependency state to build a
        // recommender with the current DAO. Any extra bind rules don't matter
        // because they could not have created any Nodes.
        Graph graph = solver.getGraph();

        // Get the set of shareable instances.
        Set<Node> shared = getShareableRoots(graph);

        // Instantiate and replace shareable nodes
        Graph pruned = instantiate(graph, shared);

        // Remove transient edges and orphaned subgraphs
        Set<Node> transientTargets = removeTransientEdges(pruned);
        removeOrphanSubgraphs(pruned, transientTargets);

        // Find the DAO node
        Set<Node> daoNodes = Sets.filter(pruned.getNodes(), new Predicate<Node>() {
            @Override
            public boolean apply(@Nullable Node input) {
                return isDAONode(input);
            }
        });
        if (daoNodes.size() > 1) {
            throw new RuntimeException("found multiple DAO nodes");
        } else if (!daoNodes.isEmpty()) {
            Node daoNode = daoNodes.iterator().next();
            pruned.replaceNode(daoNode, new Node(DAOSatisfaction.label()));
        } // otherwise, no DAO, not really a problem

        return new LenskitRecommenderEngine(factory, graph, config.getSPI());
    }

    private boolean isDAONode(@Nullable Node n) {
        CachedSatisfaction label = n == null ? null : n.getLabel();
        return label != null &&
                DataAccessObject.class.isAssignableFrom(
                        label.getSatisfaction().getErasedType());
    }

    /**
     * Prune the graph, returning the set of shared roots (objects that need
     * to be replaced with instance satisfactions in the final graph).
     *
     * @param graph The graph to analyze. The graph is not modified.
     * @return The set of root nodes - nodes that need to be instantiated and
     *         removed. These nodes are in topologically sorted order.
     */
    private LinkedHashSet<Node> getShareableRoots(Graph graph) {
        LinkedHashSet<Node> shared = new LinkedHashSet<Node>();

        List<Node> nodes = graph.sort(graph.getNode(null));
        for (Node node : nodes) {
            // TODO Be more selective about actually shareable nodes.
            if (isDAONode(node)) {
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
     * @param graph The complete configuration graph.
     * @param toReplace The shared nodes to replace.
     * @return A new graph with the nodes in {@code toReplace} replaced with instance
     *         satisfaction nodes holding the resulting instances.
     */
    private Graph instantiate(Graph graph, Set<Node> toReplace) {
        StaticInjector injector = new StaticInjector(config.getSPI(), graph);
        Graph trimmed = graph.clone();
        for (Node node: toReplace) {
            Object obj = injector.instantiate(node);
            Satisfaction sat = config.getSPI().satisfy(obj);
            Node repl = new Node(new CachedSatisfaction(sat, CachePolicy.NO_PREFERENCE));
            trimmed.replaceNode(node, repl);
        }
        return trimmed;
    }

    /**
     * Remove transient edges from a graph.
     * @param graph The graph to remove transient edges from.
     * @return The set of tail nodes of removed edges.
     */
    private Set<Node> removeTransientEdges(Graph graph) {
        Set<Node> targets = new HashSet<Node>();
        Set<Node> seen = new HashSet<Node>();
        Queue<Node> work = new LinkedList<Node>();
        work.add(graph.getNode(null));
        while (!work.isEmpty()) {
            Node node = work.remove();
            for (Edge e: graph.getOutgoingEdges(node)) {
                Node nbr = e.getTail();
                if (seen.contains(nbr)) {
                    continue;
                }
                seen.add(nbr);

                // remove transient edges, traverse non-transient ones
                if (GraphtUtils.desireIsTransient(e.getDesire())) {
                    graph.removeEdge(e);
                    targets.add(nbr);
                } else {
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
