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

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.*;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.solver.CachePolicy;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.InstanceProvider;
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
     * Groovy-compatible alias for {@link #in(Class,Class)}.
     */
    public Context within(Class<? extends Annotation> qualifier, Class<?> type) {
        return in(qualifier, type);
    }

    /**
     * Groovy-compatible alias for {@link #in(String,Class)}.
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
        } catch(SolverException e) {
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
        Graph<Pair<Satisfaction,CachePolicy>, Desire> buildGraph = solver.getGraph();
        
        // Instantiate all nodes, and remove transient edges
        Queue<Node<Pair<Satisfaction,CachePolicy>>> removeQueue =
                new LinkedList<Node<Pair<Satisfaction, CachePolicy>>>();
        Map<Node<Pair<Satisfaction, CachePolicy>>, Object> instances = instantiate(buildGraph, removeQueue);
        
        // Remove all subgraphs that have been detached by the transient edge removal
        pruneGraph(buildGraph, removeQueue);
        
        Iterator<Entry<Node<Pair<Satisfaction, CachePolicy>>, Object>> i = instances.entrySet().iterator();
        while(i.hasNext()) {
            Node<Pair<Satisfaction, CachePolicy>> n = i.next().getKey();
            if (n.getLabel() != null) {
                // Remove this instance if it is a DAO, or depends on a DAO,
                // or if no other node depends on it
                Set<Edge<Pair<Satisfaction, CachePolicy>, Desire>> incoming = buildGraph.getIncomingEdges(n);
                Pair<Satisfaction, CachePolicy> label = n.getLabel();
                assert label != null;
                if (DataAccessObject.class.isAssignableFrom(label.getLeft().getErasedType())) {
                    // This is the DAO instance node specific to the build phase,
                    // we replace it with a special satisfaction so it can be replaced
                    // per-session by the LenskitRecommenderEngine
                    Node<Pair<Satisfaction,CachePolicy>> newDAONode =
                            new Node<Pair<Satisfaction, CachePolicy>>(DAOSatisfaction.label());
                    buildGraph.replaceNode(n, newDAONode);
                    i.remove();
                } else if (incoming == null || incoming.isEmpty() || requiresDAO(n, buildGraph)) {
                    // This instance either requires a session DAO, or is no
                    // longer part of the graph
                    i.remove();
                }
            }
        }
        
        return new LenskitRecommenderEngine(factory, buildGraph, 
                                            instances, config.getSPI());
    }
    
    private boolean requiresDAO(Node<Pair<Satisfaction, CachePolicy>> n, Graph<Pair<Satisfaction, CachePolicy>, Desire> graph) {
        for (Edge<Pair<Satisfaction, CachePolicy>, Desire> e: graph.getOutgoingEdges(n)) {
            Node<Pair<Satisfaction, CachePolicy>> tail = e.getTail();
            Pair<Satisfaction, CachePolicy> label = tail.getLabel();
            assert label != null;
            if (DataAccessObject.class.isAssignableFrom(label.getLeft().getErasedType())) {
                // The node, n, has a direct dependency on a DAO
                return true;
            } else {
                // Check if it has an indirect dependency on a DAO
                if (requiresDAO(tail, graph)) {
                    return true;
                }
            }
        }
        
        // The node does not have any dependencies on a DAO
        return false;
    }
    
    private void pruneGraph(Graph<Pair<Satisfaction, CachePolicy>, Desire> graph, Queue<Node<Pair<Satisfaction, CachePolicy>>> removeQueue) {
        while(!removeQueue.isEmpty()) {
            Node<Pair<Satisfaction, CachePolicy>> candidate = removeQueue.poll();
            Set<Edge<Pair<Satisfaction, CachePolicy>, Desire>> incoming = graph.getIncomingEdges(candidate); // null if candidate got re-added
            if (incoming != null && incoming.isEmpty()) {
                // No other node depends on this node, so we can remove it,
                // we must also flag its dependencies as removal candidates
                for (Edge<Pair<Satisfaction, CachePolicy>, Desire> e: graph.getOutgoingEdges(candidate)) {
                    removeQueue.add(e.getTail());
                }
                graph.removeNode(candidate);
            }
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<Node<Pair<Satisfaction,CachePolicy>>, Object> instantiate(Graph<Pair<Satisfaction, CachePolicy>, Desire> graph, Queue<Node<Pair<Satisfaction, CachePolicy>>> removeQueue) {
        List<Node<Pair<Satisfaction, CachePolicy>>> sorted = graph.sort(graph.getNode(null));
        final Map<Node<Pair<Satisfaction, CachePolicy>>, Object> instanceMap = new HashMap<Node<Pair<Satisfaction, CachePolicy>>, Object>();

        for (Node<Pair<Satisfaction, CachePolicy>> n: sorted) {
            Pair<Satisfaction, CachePolicy> label = n.getLabel();
            if (label != null && !instanceMap.containsKey(n)) {
                // instantiate this node
                final Set<Edge<Pair<Satisfaction, CachePolicy>, Desire>> outgoing = graph.getOutgoingEdges(n);
                Provider<?> provider = label.getLeft().makeProvider(new ProviderSource() {
                    @Override
                    public Provider<?> apply(Desire desire) {
                        for (Edge<Pair<Satisfaction, CachePolicy>, Desire> e : outgoing) {
                            Desire ed = e.getLabel();
                            assert ed != null;
                            if (ed.equals(desire)) {
                                // Return the cached instance based on the tail node
                                Object instance = instanceMap.get(e.getTail());
                                return new InstanceProvider(instance);
                            }
                        }

                        // Should not happen
                        throw new RuntimeException("Could not find instantiated dependency");
                    }
                });
                
                // Store created instance into the map
                instanceMap.put(n, provider.get());
                
                // Remove all transient outgoing edges from the graph
                for (Edge<Pair<Satisfaction, CachePolicy>, Desire> e: outgoing) {
                    Desire lbl = e.getLabel();
                    assert lbl != null; // non-root dependencies never have null labels
                    Attributes attrs = lbl.getInjectionPoint().getAttributes();
                    if (attrs.getAttribute(Transient.class) != null) {
                        graph.removeEdge(e);
                        
                        // Push the tail node of the transient edge into the queue,
                        // there's a chance that it can be removed if it has no more
                        // incoming edges
                        removeQueue.add(e.getTail());
                    }
                }
            }
        }
        
        return instanceMap;
    }
}
