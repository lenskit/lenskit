/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.inject.GraphtUtils;
import org.lenskit.inject.RecommenderGraphBuilder;
import org.lenskit.inject.RecommenderInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Builds LensKit recommender engines from configurations.
 *
 * <p>
 * If multiple configurations are used, later configurations superseded previous configurations.
 * This allows you to add a configuration of defaults, followed by a custom configuration.  The
 * final build process takes the <em>union</em> of the roots of all provided configurations as
 * the roots of the configured object graph.
 * </p>
 *
 * @see LenskitConfiguration
 * @see LenskitRecommenderEngine
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitRecommenderEngineBuilder {
    private static final Logger logger = LoggerFactory.getLogger(LenskitRecommenderEngineBuilder.class);
    private ClassLoader classLoader = ClassLoaders.inferDefault(getClass());
    private List<Pair<LenskitConfiguration,ModelDisposition>> configurations = Lists.newArrayList();

    /**
     * Get the class loader this builder will use.  By default, it uses the thread's current context
     * class loader (if set).
     *
     * @return The class loader to be used.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Set the class loader to use.
     * @param loader The class loader to use when building the recommender.
     * @return The builder (for chaining).
     */
    public LenskitRecommenderEngineBuilder setClassLoader(ClassLoader loader) {
        classLoader = loader;
        return this;
    }

    /**
     * Add a configuration to be included in the recommender engine.  This is the equivalent of
     * calling {@link #addConfiguration(LenskitConfiguration, ModelDisposition)} with the {@link ModelDisposition#INCLUDED}.
     * @param config The configuration.
     * @return The builder (for chaining).
     */
    public LenskitRecommenderEngineBuilder addConfiguration(LenskitConfiguration config) {
        return addConfiguration(config, ModelDisposition.INCLUDED);
    }

    /**
     * Add a configuration to be used when building the engine.
     * @param config The configuration.
     * @param disp The disposition for this configuration.
     * @return The builder (for chaining).
     */
    public LenskitRecommenderEngineBuilder addConfiguration(LenskitConfiguration config, ModelDisposition disp) {
        configurations.add(Pair.of(config, disp));
        return this;
    }

    /**
     * Build the recommender engine.
     *
     * @return The built recommender engine, with {@linkplain ModelDisposition#EXCLUDED excluded}
     *         components removed.
     * @throws RecommenderBuildException
     * @deprecated Use {@link #build(DataAccessObject)}
     */
    @Deprecated
    public LenskitRecommenderEngine build() throws RecommenderBuildException {
        return build(null);
    }

    /**
     * Build the recommender engine.
     *
     * @param dao The data access object to use.  Can be `null` to build without a DAO, but this is only useful in
     *            special cases.
     * @return The built recommender engine, with {@linkplain ModelDisposition#EXCLUDED excluded}
     *         components removed.
     * @throws RecommenderBuildException
     */
    public LenskitRecommenderEngine build(DataAccessObject dao) throws RecommenderBuildException {
        // Build the initial graph
        logger.debug("building graph from {} configurations", configurations.size());
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.setClassLoader(classLoader);
        for (Pair<LenskitConfiguration, ModelDisposition> cfg : configurations) {
            rgb.addConfiguration(cfg.getLeft());
        }
        LenskitConfiguration daoConfig = null;
        if (dao != null) {
            daoConfig = new LenskitConfiguration();
            daoConfig.addComponent(dao);
            rgb.addConfiguration(daoConfig);
        }
        RecommenderInstantiator inst;
        try {
            inst = RecommenderInstantiator.create(rgb.buildGraph());
        } catch (ResolutionException e) {
            throw new RecommenderBuildException("Cannot resolve recommender graph", e);
        }
        DAGNode<Component, Dependency> graph = inst.instantiate();

        graph = rewriteGraph(graph, daoConfig);

        boolean instantiable = GraphtUtils.getPlaceholderNodes(graph).isEmpty();
        return new LenskitRecommenderEngine(graph, instantiable);
    }


    private DAGNode<Component, Dependency> rewriteGraph(DAGNode<Component, Dependency> graph,
                                                        LenskitConfiguration daoConfig) throws RecommenderConfigurationException {
        RecommenderGraphBuilder rewriteBuilder = new RecommenderGraphBuilder();
        boolean rewrite = false;
        for (Pair<LenskitConfiguration,ModelDisposition> cfg: configurations) {
            switch (cfg.getRight()) {
            case EXCLUDED:
                rewriteBuilder.addBindings(cfg.getLeft().getBindings());
                rewriteBuilder.addRoots(cfg.getLeft().getRoots());
                rewrite = true;
                break;
            }
        }
        if (daoConfig != null) {
            rewriteBuilder.addBindings(daoConfig.getBindings());
            rewriteBuilder.addRoots(daoConfig.getRoots());
            rewrite = true;
        }

        if (rewrite) {
            logger.debug("rewriting graph");
            DependencySolver rewriter = rewriteBuilder.buildDependencyUnsolver();
            try {
                graph = rewriter.rewrite(graph);
            } catch (ResolutionException e) {
                throw new RecommenderConfigurationException("Resolution error while rewriting graph", e);
            }
        }
        return graph;
    }
}
