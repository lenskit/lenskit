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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.*;

import static org.grouplens.grapht.BindingFunctionBuilder.RuleSet;
import static org.grouplens.lenskit.core.ContextWrapper.coerce;

/**
 * {@link RecommenderEngineFactory} that builds a LenskitRecommenderEngine.
 * <p>
 * This class is final for copying safety. This decision can be revisited.
 * </p>
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @compat Public
 */
public final class LenskitRecommenderEngineFactory extends AbstractConfigContext implements RecommenderEngineFactory {
    private static final Class<?>[] INITIAL_ROOTS = {
            RatingPredictor.class,
            ItemScorer.class,
            GlobalItemScorer.class,
            ItemRecommender.class,
            GlobalItemRecommender.class
    };

    private static final Logger logger = LoggerFactory.getLogger(LenskitRecommenderEngineFactory.class);

    private final BindingFunctionBuilder config;
    private DAOFactory factory;
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
    public <T> Binding<T> bind(Class<? extends Annotation> qualifier, Class<T> type) {
        return bind(type).withQualifier(qualifier);
    }

    @Override
    public LenskitConfigContext in(Class<?> type) {
        return coerce(config.getRootContext().in(type));
    }

    @Override
    public LenskitConfigContext in(Class<? extends Annotation> qualifier, Class<?> type) {
        return coerce(config.getRootContext().in(qualifier, type));
    }

    @Override
    public LenskitConfigContext in(Annotation qualifier, Class<?> type) {
        return coerce(config.getRootContext().in(qualifier, type));
    }

    @Override
    public LenskitConfigContext at(Class<?> type) {
        return coerce(config.getRootContext().at(type));
    }

    @Override
    public LenskitConfigContext at(Class<? extends Annotation> qualifier, Class<?> type) {
        return coerce(config.getRootContext().at(qualifier, type));
    }

    @Override
    public LenskitConfigContext at(Annotation qualifier, Class<?> type) {
        return coerce(config.getRootContext().at(qualifier, type));
    }

    /**
     * Groovy-compatible alias for {@link #in(Class)}.
     */
    @SuppressWarnings("unused")
    public LenskitConfigContext within(Class<?> type) {
        return in(type);
    }

    /**
     * Groovy-compatible alias for {@link #in(Class, Class)}.
     */

    public LenskitConfigContext within(Class<? extends Annotation> qualifier, Class<?> type) {
        return in(qualifier, type);
    }

    @Override
    public LenskitRecommenderEngineFactory clone() {
        return new LenskitRecommenderEngineFactory(this);
    }

    /**
     * Get the DAO factory.
     *
     * @return The DAO factory.
     */
    public DAOFactory getDAOFactory() {
        return factory;
    }

    /**
     * Set the DAO factory.
     *
     * @param f The new DAO factory.
     */
    public void setDAOFactory(DAOFactory f) {
        factory = f;
    }

    @Override
    public LenskitRecommenderEngine create() throws RecommenderBuildException {
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

    public LenskitRecommenderEngine create(@Nonnull DataAccessObject dao) throws RecommenderBuildException {
        Graph original;
        try {
            original = buildGraph(dao);
        } catch (RuntimeException ex) {
            throw new RecommenderConfigurationException("could not build recommender graph", ex);
        }

        // Get the set of shareable instances.
        Set<Node> shared = getShareableNodes(original);
        logger.debug("found {} shared nodes", shared.size());

        // Instantiate and replace shareable nodes
        Graph modified = original.clone();

        Set<Node> sharedInstances;
        try {
            sharedInstances = instantiate(modified, shared);
        } catch (RuntimeException ex) {
            throw new RecommenderBuildException("could not instantiate shared components", ex);
        }
        logger.debug("found {} shared instances", sharedInstances.size());

        // Remove transient edges and orphaned subgraphs
        Set<Node> transientTargets = removeTransientEdges(modified, sharedInstances);
        Set<Node> removed = removeOrphanSubgraphs(modified, transientTargets);
        logger.debug("removed {} orphaned nodes", removed.size());

        // Find the DAO node
        Node daoNode = GraphtUtils.findDAONode(modified);
        Node daoPlaceholder = null;
        if (daoNode != null) {
            daoPlaceholder = GraphtUtils.replaceNodeWithPlaceholder(config.getSPI(),
                                                                    modified, daoNode);
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
    private Set<Node> instantiate(Graph graph, Set<Node> toReplace) {
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
            replacements.add(repl);
        }
        return replacements;
    }

    /**
     * Simulate an instantiation of the shared objects in a graph.
     *
     * @param graph The complete configuration graph.
     * @return A new graph that is identical to the original graph if it were
     *         subjected to the instantiation process.
     */
    private Graph simulateInstantiation(Graph graph) {
        Graph modified = graph.clone();
        Set<Node> toReplace = getShareableNodes(modified);
        InjectSPI spi = config.getSPI();
        for (Node node : toReplace) {
            CachedSatisfaction label = node.getLabel();
            assert label != null;
            if (!label.getSatisfaction().hasInstance()) {
                Satisfaction instanceSat = spi.satisfyWithNull(label.getSatisfaction().getErasedType());
                Node repl = new Node(instanceSat, label.getCachePolicy());
                modified.replaceNode(node, repl);
            }
        }
        return modified;
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

    private Set<Node> removeOrphanSubgraphs(Graph graph, Collection<Node> candidates) {
        Set<Node> removed = new HashSet<Node>();
        Queue<Node> removeQueue = new LinkedList<Node>(candidates);
        while (!removeQueue.isEmpty()) {
            Node candidate = removeQueue.poll();
            Set<Edge> incoming = graph.getIncomingEdges(candidate); // null if candidate got re-added
            if (incoming != null && incoming.isEmpty()) {
                // No other node depends on this node, so we can remove it,
                // we must also flag its dependencies as removal candidates
                // Flag each multiple times, as it could become a candidate late
                for (Edge e : graph.getOutgoingEdges(candidate)) {
                    removeQueue.add(e.getTail());
                }
                logger.debug("removing orphan node {}", candidate);
                graph.removeNode(candidate);
                removed.add(candidate);
            }
        }
        return removed;
    }

    private Graph buildGraph(DataAccessObject dao) {
        BindingFunctionBuilder cfg = config.clone();
        if (dao == null) {
            cfg.getRootContext().bind(DataAccessObject.class).toNull();
        } else {
            cfg.getRootContext().bind(DataAccessObject.class).to(dao);
        }

        return finishBuild(cfg);
    }

    private Graph buildGraph(Class<? extends DataAccessObject> daoType) {
        BindingFunctionBuilder cfg = config.clone();
        if (daoType == null) {
            cfg.getRootContext().bind(DataAccessObject.class).toNull();
        } else {
            cfg.getRootContext().bind(DataAccessObject.class).to(daoType);
            cfg.getRootContext().bind(daoType).toNull();
        }
        return finishBuild(cfg);
    }

    private Graph finishBuild(BindingFunctionBuilder config) {
        DependencySolver solver = new DependencySolver(
                Arrays.asList(config.build(RuleSet.EXPLICIT),
                              config.build(RuleSet.INTERMEDIATE_TYPES),
                              config.build(RuleSet.SUPER_TYPES),
                              new DefaultDesireBindingFunction(config.getSPI())),
                CachePolicy.MEMOIZE,
                100);

        // Resolve all required types to complete a Recommender
        for (Class<?> root : roots) {
            resolve(root, solver);
        }

        // At this point the graph contains the dependency state to build a
        // recommender with the current DAO. Any extra bind rules don't matter
        // because they could not have created any Nodes.
        return solver.getGraph();
    }

    public Graph getInitialGraph(Class<? extends DataAccessObject> daoType) {
        return buildGraph(daoType);
    }

    public Graph getInstantiatedGraph(Class<? extends DataAccessObject> daoType) {
        return simulateInstantiation(buildGraph(daoType));
    }
}
