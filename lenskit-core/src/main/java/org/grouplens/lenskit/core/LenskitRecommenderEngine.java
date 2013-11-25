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
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.RecommenderEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * LensKit implementation of a recommender engine.  It uses containers set up by
 * the {@link LenskitConfiguration} to set up actual recommenders, and can build
 * multiple recommenders from the same model.
 * <p>If you just want to quick create a recommender for evaluation or testing,
 * consider using {@link LenskitRecommender#build(LenskitConfiguration)}.</p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @see LenskitConfiguration
 * @see LenskitRecommender
 */
public final class LenskitRecommenderEngine implements RecommenderEngine {
    private static final Logger logger = LoggerFactory.getLogger(LenskitRecommenderEngine.class);

    private final DAGNode<CachedSatisfaction, DesireChain> graph;

    private final InjectSPI spi;

    LenskitRecommenderEngine(DAGNode<CachedSatisfaction, DesireChain> dependencies, InjectSPI spi) {
        Preconditions.checkArgument(spi instanceof ReflectionInjectSPI,
                                    "SPI must be a reflection SPI");
        this.graph = dependencies;
        this.spi = spi;
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized engine from the
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
        return load(file, null);
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized engine from the
     * given file. The new engine will be identical to the old except it will use the new
     * DAOFactory. It is assumed that the file was created by using {@link #write(OutputStream)}.
     *
     * @param file   The file from which to load the engine.
     * @param loader The class loader to load from ({@code null} to use a default class loader).
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException
     *                     If the configuration cannot be used.
     */
    public static LenskitRecommenderEngine load(File file, ClassLoader loader) throws IOException, RecommenderConfigurationException {
        logger.info("Loading recommender engine from {}", file);
        FileInputStream input = new FileInputStream(file);
        try {
            return load(input, loader);
        } finally {
            input.close();
        }
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized engine from the
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
        return load(input, null);
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized engine from the
     * given input stream. The new engine will be identical to the old. It is assumed that the file
     * was created by using {@link #write(OutputStream)}.
     *
     * @param input  The stream from which to load the engine.
     * @param loader The class loader to load from ({@code null} to use a default class loader).
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException
     *                     If the configuration cannot be used.
     */
    public static LenskitRecommenderEngine load(InputStream input, ClassLoader loader) throws IOException, RecommenderConfigurationException {
        logger.debug("using classloader {}", loader);
        InjectSPI spi = new ReflectionInjectSPI();
        ObjectInputStream in = new CustomClassLoaderObjectInputStream(input, loader);
        try {
            Thread current = Thread.currentThread();
            // save the old class loader
            ClassLoader oldLoader = current.getContextClassLoader();
            if (loader != null) {
                // set the new class loader
                // Grapht will automatically use the context class loader
                current.setContextClassLoader(loader);
            }
            try {
                DAGNode<CachedSatisfaction, DesireChain> dependencies = (DAGNode) in.readObject();
                return new LenskitRecommenderEngine(dependencies, spi);
            } finally {
                if (loader != null) {
                    // restore the old class loader if needed
                    current.setContextClassLoader(oldLoader);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RecommenderConfigurationException(e);
        } finally {
            in.close();
        }
    }

    /**
     * Write the state of this LenskitRecommenderEngine to the given file so
     * that it can be recreated later using another DAOFactory. This uses
     * default object serialization so if the factory has a PicoContainer or
     * session bindings containing non-serializable types, this will fail.
     *
     * @param file The file to write the rec engine to.
     * @throws IOException if there is an error serializing the engine.
     * @see #write(java.io.OutputStream)
     */
    public void write(@Nonnull File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            write(out);
        } finally {
            out.close();
        }
    }

    /**
     * Write the state of this LenskitRecommenderEngine to the given stream so
     * that it can be recreated later using another DAOFactory. This uses
     * default object serialization so if the factory has session bindings
     * containing non-serializable types, this will fail.
     *
     * @param stream The file to write the rec engine to.
     * @throws IOException if there is an error serializing the engine.
     * @see #load(InputStream)
     */
    public void write(@Nonnull OutputStream stream) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(stream);
        try {
            out.writeObject(graph);
        } finally {
            out.close();
        }
    }

    @Override
    public LenskitRecommender createRecommender() {
        StaticInjector inj = new StaticInjector(spi, graph);
        return new LenskitRecommender(inj);
    }

    /**
     * Get the dependency graph of the recommender engine.
     *
     * @return The dependency graph.
     */
    DAGNode<CachedSatisfaction, DesireChain> getGraph() {
        return graph;
    }

    /**
     * Build a LensKit recommender engine from a configuration.  The resulting recommender is
     * independent of any subsequent modifications to the configuration.  The recommender is built
     * without a symbol mapping.
     *
     * @param config     The configuration.
     * @return The recommender engine.
     */
    public static LenskitRecommenderEngine build(LenskitConfiguration config) throws RecommenderBuildException {
        return newBuilder().addConfiguration(config).build();
    }

    /**
     * Create a new recommender engine builder.
     * @return A new recommender engine builder.
     */
    public static LenskitRecommenderEngineBuilder newBuilder() {
        return new LenskitRecommenderEngineBuilder();
    }
}
