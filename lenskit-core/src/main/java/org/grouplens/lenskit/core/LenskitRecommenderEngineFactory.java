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

import com.google.common.base.Throwables;
import org.grouplens.grapht.Binding;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.RecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * {@link RecommenderEngineFactory} that builds a LenskitRecommenderEngine.
 * <p>
 * This class is final for copying safety. This decision can be revisited.
 * </p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @deprecated Use {@link LenskitConfiguration} and {@link LenskitRecommenderEngine#build(DAOFactory, LenskitConfiguration)} instead.
 */
@Deprecated
public final class LenskitRecommenderEngineFactory extends AbstractConfigContext implements RecommenderEngineFactory {
    private static final int RESOLVE_DEPTH_LIMIT = 100;

    private final LenskitConfiguration config;
    private DAOFactory factory;

    /**
     * Create a new recommender engine factory.
     */
    public LenskitRecommenderEngineFactory() {
        this((DAOFactory) null);
    }

    /**
     * Create a new recommender engine factory.
     *
     * @param factory The DAO factory to get data access.
     */
    public LenskitRecommenderEngineFactory(@Nullable DAOFactory factory) {
        this.factory = factory;
        config = new LenskitConfiguration();

    }

    private LenskitRecommenderEngineFactory(LenskitRecommenderEngineFactory engineFactory) {
        factory = engineFactory.factory;
        config = new LenskitConfiguration(engineFactory.config);
    }

    /**
     * Add the specified component type as a root component. This forces it (and its
     * dependencies) to be resolved, and makes it available from the resulting
     * recommenders.
     *
     * @param componentType The type of component to add as a root (typically an interface).
     * @see LenskitRecommender#get(Class)
     * @see LenskitConfiguration#addRoot(Class)
     */
    public void addRoot(Class<?> componentType) {
        config.addRoot(componentType);
    }

    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return config.bind(type);
    }

    @Override
    public <T> Binding<T> bind(Class<? extends Annotation> qualifier, Class<T> type) {
        return config.bind(qualifier,  type);
    }

    @Override
    public LenskitConfigContext within(Class<?> type) {
        return config.within(type);
    }

    @Override
    public LenskitConfigContext within(Class<? extends Annotation> qualifier, Class<?> type) {
        return config.within(qualifier, type);
    }

    @Override
    public LenskitConfigContext within(Annotation qualifier, Class<?> type) {
        return config.within(qualifier, type);
    }

    @Override
    public LenskitConfigContext at(Class<?> type) {
        return config.at(type);
    }

    @Override
    public LenskitConfigContext at(Class<? extends Annotation> qualifier, Class<?> type) {
        return config.at(qualifier, type);
    }

    @Override
    public LenskitConfigContext at(Annotation qualifier, Class<?> type) {
        return config.at(qualifier, type);
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
        return LenskitRecommenderEngine.build(getDAOFactory(), config);
    }

    /**
     * Create a recommender engine with a specified DAO.
     * @param dao The DAO.
     * @return The recommender engine.
     * @throws RecommenderBuildException if there is an error building the recommender.
     */
    public LenskitRecommenderEngine create(@Nonnull DataAccessObject dao) throws RecommenderBuildException {
        return LenskitRecommenderEngine.build(dao, config);
    }

    /**
     * Simulate an instantiation of the shared objects in a graph.  This method is made public
     * only to facilitate analysis of LensKit graphs.
     *
     * @param graph The complete configuration graph.
     * @return A new graph that is identical to the original graph if it were
     *         subjected to the instantiation process.
     */
    public Graph simulateInstantiation(Graph graph) {
        return new RecommenderInstantiator(config.getSPI(), graph).simulate();
    }

    /**
     * Get a mockup of the full recommender graph. This fully resolves the graph so that
     * it can be analyzed, but does not create any objects.
     *
     * @param daoType The type of the DAO (so resolution can be successful in the face of
     *                dependencies on DAO subtypes).
     * @return The full graph.
     */
    public Graph getInitialGraph(Class<? extends DataAccessObject> daoType) {
        try {
            return config.buildGraph(daoType);
        } catch (RecommenderConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Get a mockup of the instantiated (per-session) recommender graph. This fully resolves the
     * graph so that it can be analyzed, but does not create any objects.
     *
     * @param daoType The type of the DAO (so resolution can be successful in the face of
     *                dependencies on DAO subtypes).
     * @return The recommender graph.
     */
    public Graph getInstantiatedGraph(Class<? extends DataAccessObject> daoType) {
        return simulateInstantiation(getInitialGraph(daoType));
    }
}
