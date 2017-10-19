/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
 * If multiple configurations are used, later configurations superseded previous configurations.
 * This allows you to add a configuration of defaults, followed by a custom configuration.  The
 * final build process takes the _union_ of the roots of all provided configurations as
 * the roots of the configured object graph.
 *
 * While this class is subclassable, and exposes protected methods, users should not subclass it.
 * Subclassing is supported only to enable the evaluator.
 *
 * @see LenskitConfiguration
 * @see LenskitRecommenderEngine
 * @since 2.1
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
     * @throws RecommenderBuildException if there is an error building the recommender.
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
     * @throws RecommenderBuildException if there is an error building the engine.
     */
    public LenskitRecommenderEngine build(DataAccessObject dao) throws RecommenderBuildException {
        DAGNode<Component, Dependency> graph = buildRecommenderGraph(dao);
        graph = instantiateGraph(graph);
        graph = rewriteExcludedComponents(graph, dao);

        boolean instantiable = GraphtUtils.getPlaceholderNodes(graph).isEmpty();
        return new LenskitRecommenderEngine(graph, instantiable);
    }

    /**
     * Build the recommender directly, skipping an engine.  This does not separate the recommender from
     * the DAO, it just directly builds it.
     *
     * @param dao The data access object to use.  Can be `null` to build without a DAO, but this is only useful in
     *            special cases.
     * @return The built recommender engine, with {@linkplain ModelDisposition#EXCLUDED excluded}
     *         components removed.
     * @throws RecommenderBuildException if there is an error building the engine.
     */
    public LenskitRecommender buildRecommender(DataAccessObject dao) throws RecommenderBuildException {
        DAGNode<Component, Dependency> graph = buildRecommenderGraph(dao);
        graph = instantiateGraph(graph);
        return new LenskitRecommender(graph);
    }

    /**
     * Build a recommender graph for this engine builder's configuration.
     *
     * Clients generally do not need to use this; it is exposed for the evaluator.
     *
     * @param dao The DAO, if available.
     * @return The graph.
     */
    protected DAGNode<Component, Dependency> buildRecommenderGraph(DataAccessObject dao) {
        logger.debug("building graph from {} configurations", configurations.size());
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.setClassLoader(classLoader);
        for (Pair<LenskitConfiguration, ModelDisposition> cfg : configurations) {
            rgb.addConfiguration(cfg.getLeft());
        }
        LenskitConfiguration daoConfig;
        if (dao != null) {
            daoConfig = new LenskitConfiguration();
            daoConfig.addComponent(dao);
            rgb.addConfiguration(daoConfig);
        }

        DAGNode<Component, Dependency> graph;
        try {
            graph = rgb.buildGraph();
        } catch (ResolutionException e) {
            throw new RecommenderBuildException("Cannot resolve recommender graph", e);
        }

        return graph;
    }

    /**
     * Instantiate the recommender graph.
     * @param graph The recommender graph.
     * @return The instantiated graph.
     */
    protected DAGNode<Component, Dependency> instantiateGraph(DAGNode<Component, Dependency> graph) {
        RecommenderInstantiator inst = RecommenderInstantiator.create(graph);

        graph = inst.instantiate();
        return graph;
    }

    /**
     * Remove configuration that should be excluded from engine graphs, particularly the DAO.
     *
     * @param graph The input graph.
     * @param dao The DAO.
     * @return The rewritten graph.
     * @throws RecommenderConfigurationException If there is a configuration error during the rewrite.
     */
    private DAGNode<Component, Dependency> rewriteExcludedComponents(DAGNode<Component, Dependency> graph,
                                                                     DataAccessObject dao) throws RecommenderConfigurationException {
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
        if (dao != null) {
            LenskitConfiguration cfg = new LenskitConfiguration();
            cfg.addComponent(dao);
            rewriteBuilder.addBindings(cfg.getBindings());
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
