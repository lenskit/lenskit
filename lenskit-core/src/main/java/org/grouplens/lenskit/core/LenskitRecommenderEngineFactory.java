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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.grouplens.grapht.Binding;
import org.grouplens.grapht.Context;
import org.grouplens.grapht.InjectorConfigurationBuilder;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Function;
import org.grouplens.grapht.util.InstanceProvider;
import org.grouplens.lenskit.GlobalItemRecommender;
import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;

/**
 * {@link RecommenderEngineFactory} that builds a LenskitRecommenderEngine.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class LenskitRecommenderEngineFactory implements RecommenderEngineFactory, Cloneable, Context {
    private final InjectorConfigurationBuilder config;
    private final DAOFactory factory;
    
    public LenskitRecommenderEngineFactory() {
        this((DAOFactory) null);
    }
    
    public LenskitRecommenderEngineFactory(@Nullable DAOFactory factory) {
        this.factory = factory;
        config = new InjectorConfigurationBuilder();
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
    public Context in(String name, Class<?> type) {
        return config.getRootContext().in(name, type);
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
        if (factory == null)
            throw new IllegalStateException("create() called with no DAOFactory");
        DataAccessObject dao = factory.snapshot();
        try {
            return create(dao);
        } finally {
            dao.close();
        }
    }
    
    private void resolve(Class<?> type, DependencySolver solver) {
        solver.resolve(solver.getSPI().desire(null, type, true));
    }
    
    public LenskitRecommenderEngine create(DataAccessObject dao) {
        InjectorConfigurationBuilder config = this.config.clone();
        config.getRootContext().bind(DataAccessObject.class).to(dao);
        
        DependencySolver solver = new DependencySolver(config.build(), 100);
        
        // Resolve all required types to complete a Recommender
        resolve(RatingPredictor.class, solver);
        resolve(ItemScorer.class, solver);
        resolve(GlobalItemScorer.class, solver);
        resolve(ItemRecommender.class, solver);
        resolve(GlobalItemRecommender.class, solver);

        // At this point the graph contains the dependency state to build a
        // recommender with the current DAO. Any extra bind rules don't matter
        // because they could not have created any Nodes.
        Graph<Satisfaction, Desire> buildGraph = solver.getGraph();
        
        // Instantiate all nodes, and remove transient edges
        Queue<Node<Satisfaction>> removeQueue = new LinkedList<Node<Satisfaction>>();
        Map<Node<Satisfaction>, Object> instances = instantiate(buildGraph, removeQueue);
        
        // Remove all subgraphs that have been detached by the transient edge removal
        pruneGraph(buildGraph, removeQueue);
        
        Iterator<Entry<Node<Satisfaction>, Object>> i = instances.entrySet().iterator();
        while(i.hasNext()) {
            Node<Satisfaction> n = i.next().getKey();
            if (n.getLabel() != null) {
                // Remove this instance if it is a DAO, or depends on a DAO,
                // or if no other node depends on it
                Set<Edge<Satisfaction, Desire>> incoming = buildGraph.getIncomingEdges(n);
                
                if (DataAccessObject.class.isAssignableFrom(n.getLabel().getErasedType())) {
                    // This is the DAO instance node specific to the build phase,
                    // we replace it with a special satisfaction so it can be replaced
                    // per-session by the LenskitRecommenderEngine
                    Node<Satisfaction> newDAONode = new Node<Satisfaction>(new DAOSatisfaction());
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
    
    private boolean requiresDAO(Node<Satisfaction> n, Graph<Satisfaction, Desire> graph) {
        for (Edge<Satisfaction, Desire> e: graph.getOutgoingEdges(n)) {
            Node<Satisfaction> tail = e.getTail();
            if (DataAccessObject.class.isAssignableFrom(tail.getLabel().getErasedType())) {
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
    
    private void pruneGraph(Graph<Satisfaction, Desire> graph, Queue<Node<Satisfaction>> removeQueue) {
        while(!removeQueue.isEmpty()) {
            Node<Satisfaction> candidate = removeQueue.poll();
            Set<Edge<Satisfaction, Desire>> incoming = graph.getIncomingEdges(candidate); // null if candidate got re-added
            if (incoming != null && incoming.isEmpty()) {
                // No other node depends on this node, so we can remove it,
                // we must also flag its dependencies as removal candidates
                for (Edge<Satisfaction, Desire> e: graph.getOutgoingEdges(candidate)) {
                    removeQueue.add(e.getTail());
                }
                graph.removeNode(candidate);
            }
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<Node<Satisfaction>, Object> instantiate(Graph<Satisfaction, Desire> graph, Queue<Node<Satisfaction>> removeQueue) {
        List<Node<Satisfaction>> sorted = graph.sort(graph.getNode(null));
        final Map<Node<Satisfaction>, Object> instanceMap = new HashMap<Node<Satisfaction>, Object>();

        for (Node<Satisfaction> n: sorted) {
            if (n.getLabel() != null && !instanceMap.containsKey(n)) {
                // instantiate this node
                final Set<Edge<Satisfaction, Desire>> outgoing = graph.getOutgoingEdges(n);
                Provider<?> provider = n.getLabel().makeProvider(new Function<Desire, Provider<?>>() {
                    @Override
                    public Provider<?> apply(Desire desire) {
                        for (Edge<Satisfaction, Desire> e: outgoing) {
                            if (e.getLabel().equals(desire)) {
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
                for (Edge<Satisfaction, Desire> e: outgoing) {
                    if (e.getLabel().isTransient()) {
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
