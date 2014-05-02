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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.util.ClassLoaderContext;
import org.grouplens.grapht.util.ClassLoaders;
import org.grouplens.lenskit.inject.GraphtUtils;
import org.grouplens.lenskit.inject.RecommenderGraphBuilder;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.grouplens.lenskit.util.io.CustomClassLoaderObjectInputStream;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.WillClose;
import java.io.*;
import java.util.List;

/**
 * Load a pre-built recommender engine from a file.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitRecommenderEngineLoader {
    private static final Logger logger = LoggerFactory.getLogger(LenskitRecommenderEngineLoader.class);
    private ClassLoader classLoader;
    private List<LenskitConfiguration> configurations = Lists.newArrayList();
    private EngineValidationMode validationMode = EngineValidationMode.IMMEDIATE;
    private CompressionMode compressionMode = CompressionMode.AUTO;

    /**
     * Get the configured class loader.
     * @return The class loader that will be used when loading the engine.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Set the class loader to use when reading the engine.
     * @param classLoader The class loader to use.
     * @return The loader (for chaining).
     */
    public LenskitRecommenderEngineLoader setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Add a configuration to use when loading the configuration.  The loaded graph will be
     * post-processed to add in components bound by this configuration.  If the engine was saved
     * with excluded configurations, then this method should be used to provide configurations to
     * reinstate any objects excluded from the saved model.  The loader will throw an exception if
     * the loaded model has any unresolved placeholders.
     *
     * @param config The configuration to add.
     * @return The loader (for chaining).
     */
    public LenskitRecommenderEngineLoader addConfiguration(LenskitConfiguration config) {
        configurations.add(config);
        return this;
    }

    /**
     * Set the validation mode for loading the recommender engine.  The default mode is
     * {@link EngineValidationMode#IMMEDIATE}.
     * @param mode The validation mode.
     * @return The loader (for chaining).
     */
    public LenskitRecommenderEngineLoader setValidationMode(EngineValidationMode mode) {
        Preconditions.checkNotNull(mode, "validation mode");
        validationMode = mode;
        return this;
    }

    /**
     * Set the compression mode to use.  The default is {@link CompressionMode#AUTO}.
     * @param comp The compression mode.
     */
    public void setCompressionMode(CompressionMode comp) {
        compressionMode = comp;
    }

    /**
     * Load a recommender engine from an input stream.
     * <p>
     * <strong>Note:</strong> this method is only capable of auto-detecting gzip-compressed data.
     * If the {@linkplain #setCompressionMode(CompressionMode) compression mode} is {@link CompressionMode#AUTO},
     * only gzip-compressed streams are supported.  Set the compression mode manually if you are
     * using XZ compression.
     * </p>
     *
     * @param stream The input stream.
     * @return The deserialized recommender.
     * @throws IOException if there is an error reading the input data.
     * @throws RecommenderConfigurationException
     *                     if there is a configuration error with the deserialized recommender or
     *                     the configurations applied to it.
     */
    public LenskitRecommenderEngine load(@WillClose InputStream stream) throws IOException, RecommenderConfigurationException {
        InputStream decomp;
        if (compressionMode == CompressionMode.AUTO) {
            decomp = LKFileUtils.transparentlyDecompress(stream);
        } else {
            decomp = compressionMode.wrapInput(stream);
        }
        return loadInternal(decomp);
    }

    /**
     * Load a recommender from a file.
     *
     * @param file The recommender model file to load.
     * @return The recommender engine.
     * @throws IOException if there is an error reading the input data.
     * @throws RecommenderConfigurationException
     *                     if there is a configuration error with the deserialized recommender or
     *                     the configurations applied to it.
     */
    public LenskitRecommenderEngine load(File file) throws IOException, RecommenderConfigurationException {
        logger.info("Loading recommender engine from {}", file);
        FileInputStream input = new FileInputStream(file);
        try {
            CompressionMode effComp = compressionMode.getEffectiveCompressionMode(file.getName());
            logger.info("using {} compression", effComp);
            return loadInternal(effComp.wrapInput(input));
        } finally {
            input.close();
        }
    }

    private LenskitRecommenderEngine loadInternal(InputStream stream) throws IOException, RecommenderConfigurationException {
        logger.debug("using classloader {}", classLoader);
        DAGNode<Component, Dependency> graph;

        // And load the stream once we've wrapped it appropriately.
        ObjectInputStream in = new CustomClassLoaderObjectInputStream(
                LKFileUtils.transparentlyDecompress(stream), classLoader);
        try {
            ClassLoaderContext ctx = null;
            if (classLoader != null) {
                // Grapht will automatically use the context class loader, set it up
                ctx = ClassLoaders.pushContext(classLoader);
            }
            try {
                graph = (DAGNode) in.readObject();
            } finally {
                if (ctx != null) {
                    ctx.pop();
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RecommenderConfigurationException(e);
        } finally {
            in.close();
        }

        if (!configurations.isEmpty()) {
            logger.info("rewriting with {} configurations", configurations.size());
            RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
            for (LenskitConfiguration config : configurations) {
                rgb.addBindings(config.getBindings());
            }
            DependencySolver solver = rgb.buildDependencySolver();
            try {
                graph = solver.rewrite(graph);
            } catch (SolverException e) {
                throw new RecommenderConfigurationException("resolution error occured while rewriting recommender", e);
            }
        }

        boolean instantiable = true;
        switch (validationMode) {
        case IMMEDIATE:
            GraphtUtils.checkForPlaceholders(graph, logger);
            break;
        case DEFERRED:
            instantiable = GraphtUtils.getPlaceholderNodes(graph).isEmpty();
            break;
        case NONE:
            break; /* do nothing, mark it as instantiable. */
        }

        return new LenskitRecommenderEngine(graph, instantiable);
    }
}
