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

import org.grouplens.grapht.Binding;
import org.grouplens.grapht.BindingFunctionBuilder;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.lenskit.*;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.grouplens.lenskit.core.ContextWrapper.coerce;

/**
 * A LensKit algorithm configuration.  Once you have a configuration, you can pass it to
 * {@link LenskitRecommenderEngine#build(org.grouplens.lenskit.data.dao.DAOFactory, LenskitConfiguration)}
 * to build a recommender engine.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public class LenskitConfiguration extends AbstractConfigContext {
    private static final int RESOLVE_DEPTH_LIMIT = 100;
    private static final Class<?>[] INITIAL_ROOTS = {
            RatingPredictor.class,
            ItemScorer.class,
            GlobalItemScorer.class,
            ItemRecommender.class,
            GlobalItemRecommender.class
    };

    private final BindingFunctionBuilder bindings;
    private final Set<Class<?>> roots;

    public LenskitConfiguration() {
        bindings = new BindingFunctionBuilder();
        roots = new HashSet<Class<?>>();
        Collections.addAll(roots, INITIAL_ROOTS);
    }

    /**
     * Create a new copy of a LensKit configuration.
     * @param other The configuration to copy.
     */
    public LenskitConfiguration(LenskitConfiguration other) {
        bindings = other.bindings.clone();
        roots = new HashSet<Class<?>>(other.roots);
    }

    /**
     * Convenience method to copy a LensKit configuration.
     * @return An independent copy of this configuration.
     */
    public LenskitConfiguration copy() {
        return new LenskitConfiguration(this);
    }

    InjectSPI getSPI() {
        return bindings.getSPI();
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
        return bindings.getRootContext().bind(type);
    }

    @Override
    public <T> Binding<T> bind(Class<? extends Annotation> qualifier, Class<T> type) {
        return bind(type).withQualifier(qualifier);
    }

    @Override
    public LenskitConfigContext within(Class<?> type) {
        return coerce(bindings.getRootContext().within(type));
    }

    @Override
    public LenskitConfigContext within(Class<? extends Annotation> qualifier, Class<?> type) {
        return coerce(bindings.getRootContext().within(qualifier, type));
    }

    @Override
    public LenskitConfigContext within(Annotation qualifier, Class<?> type) {
        return coerce(bindings.getRootContext().within(qualifier, type));
    }

    @Override
    public LenskitConfigContext at(Class<?> type) {
        return coerce(bindings.getRootContext().at(type));
    }

    @Override
    public LenskitConfigContext at(Class<? extends Annotation> qualifier, Class<?> type) {
        return coerce(bindings.getRootContext().at(qualifier, type));
    }

    @Override
    public LenskitConfigContext at(Annotation qualifier, Class<?> type) {
        return coerce(bindings.getRootContext().at(qualifier, type));
    }

    private void resolve(Class<?> type, DependencySolver solver) throws SolverException {
        solver.resolve(bindings.getSPI().desire(null, type, true));
    }

    private Graph resolveGraph(BindingFunctionBuilder cfg) throws SolverException {
        DependencySolver solver = new DependencySolver(
                Arrays.asList(cfg.build(BindingFunctionBuilder.RuleSet.EXPLICIT),
                              cfg.build(BindingFunctionBuilder.RuleSet.INTERMEDIATE_TYPES),
                              cfg.build(BindingFunctionBuilder.RuleSet.SUPER_TYPES),
                              new DefaultDesireBindingFunction(cfg.getSPI())),
                CachePolicy.MEMOIZE, RESOLVE_DEPTH_LIMIT);

        // Resolve all required types to complete a Recommender
        for (Class<?> root : roots) {
            resolve(root, solver);
        }

        // At this point the graph contains the dependency state to build a
        // recommender with the current DAO. Any extra bind rules don't matter
        // because they could not have created any Nodes.
        return solver.getGraph();
    }

    /**
     * Get a mockup of the full recommender graph. This fully resolves the graph so that
     * it can be analyzed, but does not create any objects.
     *
     * @param daoType The type of the DAO (so resolution can be successful in the face of
     *                dependencies on DAO subtypes).
     * @return The full graph.
     */
    public Graph buildGraph(Class<? extends DataAccessObject> daoType) throws RecommenderConfigurationException {
        BindingFunctionBuilder cfg = bindings.clone();
        if (daoType == null) {
            cfg.getRootContext().bind(DataAccessObject.class).toNull();
        } else {
            cfg.getRootContext().bind(DataAccessObject.class).to(daoType);
            cfg.getRootContext().bind(daoType).toNull();
        }
        try {
            return resolveGraph(cfg);
        } catch (SolverException e) {
            throw new RecommenderConfigurationException("Cannot resolve configuration graph", e);
        }
    }

    /**
     * Get a mockup of the full recommender graph. This fully resolves the graph so that
     * it can be analyzed, but does not create any objects.  Use {@link #buildGraph(Class)} if your
     * configuration depends on a custom DAO subclass.
     *
     * @return The full graph.
     */
    public Graph buildGraph() throws RecommenderConfigurationException {
        return buildGraph(DataAccessObject.class);
    }

    /**
     * Get the full recommender graph with a live DAO.  This does not instantiate any other nodes.
     *
     * @param dao The DAO object.
     * @return The full configuration graph with the live DAO in place.
     */
    public Graph buildGraph(DataAccessObject dao) throws RecommenderConfigurationException {
        BindingFunctionBuilder cfg = bindings.clone();
        if (dao == null) {
            cfg.getRootContext().bind(DataAccessObject.class).toNull();
        } else {
            cfg.getRootContext().bind(DataAccessObject.class).to(dao);
        }

        try {
            return resolveGraph(cfg);
        } catch (SolverException e) {
            throw new RecommenderConfigurationException("Cannot resolve configuration graph", e);
        }
    }
}
