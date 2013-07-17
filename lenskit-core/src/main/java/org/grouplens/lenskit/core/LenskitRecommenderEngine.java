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
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.RecommenderEngine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;

/**
 * LensKit implementation of a recommender engine.  It uses containers set up by
 * the {@link LenskitConfiguration} to set up recommender sessions.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @see LenskitConfiguration
 * @see LenskitRecommender
 */
public final class LenskitRecommenderEngine implements RecommenderEngine {
    private final Graph dependencies;
    private final Node rootNode;

    private final InjectSPI spi;
    private SymbolMapping symbolMapping;

    LenskitRecommenderEngine(Graph dependencies, InjectSPI spi, @Nonnull SymbolMapping map) {
        Preconditions.checkArgument(spi instanceof ReflectionInjectSPI,
                                    "SPI must be a reflection SPI");
        this.dependencies = dependencies;
        this.spi = spi;

        rootNode = dependencies.getNode(null);
        symbolMapping = map;
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized
     * engine from the given file. The new engine will be identical to the old
     * except it will use the new DAOFactory. It is assumed that the file was
     * created by using {@link #write(OutputStream)}.
     *
     * @param file The file from which to load the engine.
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException If the configuration cannot be used.
     */
    public static LenskitRecommenderEngine load(File file) throws IOException, RecommenderConfigurationException {
        FileInputStream input = new FileInputStream(file);
        try {
            return load(input);
        } finally {
            input.close();
        }
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized engine from the
     * given input stream. The new engine will be identical to the old. It is assumed that the file
     * was created by using {@link #write(OutputStream)}.
     *
     * @param input The stream from which to load the engine.
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException
     *                     If the configuration cannot be used.
     */
    public static LenskitRecommenderEngine load(InputStream input) throws IOException, RecommenderConfigurationException {
        InjectSPI spi = new ReflectionInjectSPI();
        ObjectInputStream in = new ObjectInputStream(input);
        try {
            Graph dependencies = (Graph) in.readObject();
            SymbolMapping map = (SymbolMapping) in.readObject();
            return new LenskitRecommenderEngine(dependencies, spi, map);
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
            out.writeObject(dependencies);
            out.writeObject(symbolMapping);
        } finally {
            out.close();
        }
    }

    /**
     * Get the current symbol mapping for this engine.
     * @return The symbol mapping for the recommnder engine.
     */
    public SymbolMapping getSymbolMapping() {
        return symbolMapping;
    }

    /**
     * Set the symbol mapping to be used by this engine.  This should be set to {@code null} before
     * serializing a recommender.
     *
     * @param mapping The symbol mapping.
     */
    public void setSymbolMapping(@Nullable SymbolMapping mapping) {
        if (mapping == null) {
            symbolMapping = SymbolMapping.empty();
        } else {
            symbolMapping = mapping;
        }
    }

    @Override
    public LenskitRecommender createRecommender() {
        return createRecommender(symbolMapping);
    }

    /**
     * Construct a recommender with a specified symbol mapping.
     * @param map The symbol mapping to use. This overrides {@link #getSymbolMapping()}.
     * @return The recommender.
     */
    public LenskitRecommender createRecommender(SymbolMapping map) {
        Injector inj = new StaticInjector(spi, dependencies, rootNode, map);
        return new LenskitRecommender(inj);
    }

    /**
     * Get the dependency graph of the recommender engine.
     *
     * @return The dependency graph.
     */
    Graph getDependencies() {
        return dependencies;
    }

    /**
     * Build a LensKit recommender engine from a configuration.  The resulting recommender is
     * independent of any subsequent modifications to the configuration.
     *
     * @param config     The configuration.
     * @param map The symbol mapping to use for the engine build.  Once the engine is built, this
     *            also its {@linkplain #setSymbolMapping(SymbolMapping) symbol mapping}; you might
     *            want to change that if you're going to serialize.
     * @return The recommender engine.
     */
    public static LenskitRecommenderEngine build(LenskitConfiguration config, SymbolMapping map) throws RecommenderBuildException {
        if (map == null) {
            map = SymbolMapping.empty();
        }
        Graph graph = RecommenderInstantiator.forConfig(config).instantiate(map);
        return new LenskitRecommenderEngine(graph, config.getSPI(), map);
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
        return build(config, SymbolMapping.empty());
    }
}
