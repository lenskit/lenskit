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

import com.google.common.base.Preconditions;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Qualifiers;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.internal.InstanceSatisfaction;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.inject.GraphtUtils;
import org.lenskit.inject.RecommenderGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.WillNotClose;
import java.io.*;

/**
 * LensKit implementation of a recommender engine.  It uses containers set up by
 * the {@link LenskitConfiguration} to set up actual recommenders, and can build
 * multiple recommenders from the same model.
 *
 * If you just want to quick create a recommender for evaluation or testing,
 * consider using {@link LenskitRecommender#build(LenskitConfiguration)}.  For more
 * control, use {@link LenskitRecommenderEngineBuilder}; to load a pre-built recommender
 * engine, use {@link LenskitRecommenderEngineLoader}.
 *
 * @compat Public
 * @see LenskitConfiguration
 * @see LenskitRecommender
 * @see LenskitRecommenderEngineBuilder
 * @see LenskitRecommenderEngineLoader
 */
public final class LenskitRecommenderEngine implements RecommenderEngine, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LenskitRecommenderEngine.class);

    private final DAGNode<Component, Dependency> graph;
    private final boolean instantiable;

    LenskitRecommenderEngine(@Nonnull DAGNode<Component,Dependency> graph,
                             boolean instantiable) {
        Preconditions.checkNotNull(graph, "configuration graph");
        this.graph = graph;
        this.instantiable = instantiable;
    }

    /**
     * Create a new recommender engine by reading a previously serialized engine from the
     * given file. The new engine will be identical to the old except it will use the new
     * DAOFactory. It is assumed that the file was created by using {@link #write(OutputStream)}.
     * Classes will be loaded using a default class loader.
     *
     * @param file The file from which to load the engine.
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException
     *                     If the configuration cannot be used.
     */
    public static LenskitRecommenderEngine load(File file) throws IOException, RecommenderConfigurationException {
        return newLoader().load(file);
    }

    /**
     * Create a new recommender engine by reading a previously serialized engine from the
     * given file. The new engine will be identical to the old except it will use the new
     * DAOFactory. It is assumed that the file was created by using {@link #write(OutputStream)}.
     *
     * @param file   The file from which to load the engine.
     * @param loader The class loader to load from ({@code null} to use a default class loader).
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException
     *                     If the configuration cannot be used.
     * @deprecated Use {@link LenskitRecommenderEngineLoader} for sophisticated loading.
     */
    @Deprecated
    public static LenskitRecommenderEngine load(File file, ClassLoader loader) throws IOException, RecommenderConfigurationException {
        return newLoader().setClassLoader(loader).load(file);
    }

    /**
     * Create a new recommender engine by reading a previously serialized engine from the
     * given input stream. The new engine will be identical to the old. It is assumed that the file
     * was created by using {@link #write(OutputStream)}.  Classes will be loaded using a default
     * class loader.
     *
     * @param input The stream from which to load the engine.
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException
     *                     If the configuration cannot be used.
     */
    public static LenskitRecommenderEngine load(InputStream input) throws IOException, RecommenderConfigurationException {
        return newLoader().load(input);
    }

    /**
     * Write the state of this recommender engine to the given file so
     * that it can be recreated later using another DAOFactory. This uses
     * default object serialization so if the factory has a PicoContainer or
     * session bindings containing non-serializable types, this will fail.
     *
     * @param file The file to write the rec engine to.
     * @throws IOException if there is an error serializing the engine.
     * @see #write(OutputStream)
     */
    public void write(@Nonnull File file) throws IOException {
        write(file, CompressionMode.NONE);
    }

    /**
     * Write the state of this recommender engine to the given file so
     * that it can be recreated later using another DAOFactory. This uses
     * default object serialization so if the factory has a PicoContainer or
     * session bindings containing non-serializable types, this will fail.
     *
     * @param file The file to write the rec engine to.
     * @param compressed Whether to compress the output file.
     * @throws IOException if there is an error serializing the engine.
     * @see #write(OutputStream)
     */
    public void write(@Nonnull File file, CompressionMode compressed) throws IOException {
        try (OutputStream out = new FileOutputStream(file);
             OutputStream zout = compressed.getEffectiveCompressionMode(file.getName()).wrapOutput(out)) {
            write(zout);
        }
    }

    /**
     * Write the state of this recommender engine to the given stream so
     * that it can be recreated later using another DAOFactory. This uses
     * default object serialization so if the factory has session bindings
     * containing non-serializable types, this will fail.
     *
     * @param stream The file to write the rec engine to.
     * @throws IOException if there is an error serializing the engine.
     * @see #load(InputStream)
     */
    public void write(@Nonnull @WillClose OutputStream stream) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(stream)) {
            out.writeObject(graph);
        }
    }

    /**
     * Create a recommender.
     * @return The recommender
     * @deprecated Use {@link #createRecommender(DataAccessObject)}
     */
    @Deprecated
    @Override
    public LenskitRecommender createRecommender() {
        Preconditions.checkState(instantiable, "recommender engine does not have instantiable graph");
        return new LenskitRecommender(graph);
    }

    /**
     * Construct a recommender with some additional configuration.  This can be used to do things
     * like add data source configuration on a per-recommender, rather than per-engine, basis.
     *
     * This method is only used for special cases needing detailed access to the recommender.
     *
     * @param config The configuration to adjust the recommender.
     * @return The constructed recommender.
     * @throws RecommenderConfigurationException if there is an error configuring the recommender.
     */
    public LenskitRecommender createRecommender(LenskitConfiguration config) throws RecommenderConfigurationException {
        final DAGNode<Component, Dependency> toBuild = createRecommenderGraph(config);

        return new LenskitRecommender(toBuild);
    }

    /**
     * Create a LensKit recommender.
     * @param dao The data access object
     * @return The constructed recommender.
     */
    public LenskitRecommender createRecommender(@WillNotClose DataAccessObject dao) throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.addComponent(dao);
        return createRecommender(config);
    }

    public DAGNode<Component, Dependency> createRecommenderGraph(LenskitConfiguration config) throws RecommenderConfigurationException {
        Preconditions.checkNotNull(config, "extra configuration");
        final DAGNode<Component, Dependency> toBuild;
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.addBindings(config.getBindings());
        DependencySolver solver = rgb.buildDependencySolver();
        try {
            toBuild = solver.rewrite(graph);
        } catch (ResolutionException ex) {
            throw new RecommenderConfigurationException("error reconfiguring recommender", ex);
        }
        GraphtUtils.checkForPlaceholders(toBuild, logger);
        return toBuild;
    }

    /**
     * Query whether this engine is instantiable.  Instantiable recommenders have all their
     * placeholders removed and are ready to instantiate.
     * @return {@code true} if the recommender is instantiable.
     */
    public boolean isInstantiable() {
        return instantiable;
    }

    /**
     * Get the dependency graph of the recommender engine.
     *
     * @return The dependency graph.
     */
    @Nonnull
    public DAGNode<Component, Dependency> getGraph() {
        return graph;
    }

    /**
     * Get the component of a particular type, if one is already instantiated.  This is useful to extract pre-built
     * models from serialized recommender engines, for example.
     * @param type The required component type.
     * @param <T> The required component type.
     * @return The component instance, or {@code null} if no instance can be retreived (either because no such
     * component is configured, or it is not yet instantiated).
     */
    @Nullable
    public <T> T getComponent(Class<T> type) {
        DAGNode<Component, Dependency> node = GraphtUtils.findSatisfyingNode(graph, Qualifiers.matchDefault(), type);
        if (node == null) {
            return null;
        }
        Satisfaction sat = node.getLabel().getSatisfaction();
        if (sat instanceof InstanceSatisfaction) {
            return type.cast(((InstanceSatisfaction) sat).getInstance());
        } else {
            return null;
        }
    }

    /**
     * Build a LensKit recommender engine from a configuration.  The resulting recommender is
     * independent of any subsequent modifications to the configuration.
     *
     * @param config     The configuration.
     * @return The recommender engine.
     */
    @SuppressWarnings("deprecation")
    public static LenskitRecommenderEngine build(LenskitConfiguration config) throws RecommenderBuildException {
        return newBuilder().addConfiguration(config).build();
    }

    /**
     * Build a LensKit recommender engine from a configuration.  The resulting recommender is
     * independent of any subsequent modifications to the configuration.
     *
     * @param config     The configuration.
     * @param dao The data access object
     * @return The recommender engine.
     */
    public static LenskitRecommenderEngine build(LenskitConfiguration config, DataAccessObject dao) throws RecommenderBuildException {
        return newBuilder().addConfiguration(config).build(dao);
    }

    /**
     * Create a new recommender engine builder.
     * @return A new recommender engine builder.
     */
    public static LenskitRecommenderEngineBuilder newBuilder() {
        return new LenskitRecommenderEngineBuilder();
    }

    /**
     * Create a new recommender engine loader.
     * @return A new recommender engine loader.
     */
    public static LenskitRecommenderEngineLoader newLoader() {
        return new LenskitRecommenderEngineLoader();
    }
}
