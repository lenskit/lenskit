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
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public LenskitRecommenderEngine load(InputStream stream) throws IOException, RecommenderConfigurationException {
        logger.debug("using classloader {}", classLoader);
        DAGNode<CachedSatisfaction, DesireChain> graph;

        InjectSPI spi = new ReflectionInjectSPI();
        ObjectInputStream in = new CustomClassLoaderObjectInputStream(stream, classLoader);
        try {
            Thread current = Thread.currentThread();
            // save the old class loader
            ClassLoader oldLoader = current.getContextClassLoader();
            if (classLoader != null) {
                // set the new class loader
                // Grapht will automatically use the context class loader
                current.setContextClassLoader(classLoader);
            }
            try {
                graph = (DAGNode) in.readObject();
            } finally {
                if (classLoader != null) {
                    // restore the old class loader if needed
                    current.setContextClassLoader(oldLoader);
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
            for (LenskitConfiguration config: configurations) {
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

        return new LenskitRecommenderEngine(graph, spi, instantiable);
    }

    public LenskitRecommenderEngine load(File file) throws IOException, RecommenderConfigurationException {
        logger.info("Loading recommender engine from {}", file);
        FileInputStream input = new FileInputStream(file);
        try {
            return load(input);
        } finally {
            input.close();
        }
    }
}
