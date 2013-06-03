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
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.grouplens.lenskit.RecommenderEngine;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * LensKit implementation of a recommender engine.  It uses containers set up by
 * the {@link LenskitRecommenderEngineFactory} to set up recommender sessions.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @see LenskitRecommenderEngineFactory
 * @see LenskitRecommender
 */
public class LenskitRecommenderEngine implements RecommenderEngine {
    private final Graph dependencies;
    private final Node rootNode;
    private final Node daoPlaceholder;

    private final InjectSPI spi;

    private final DAOFactory factory;

    LenskitRecommenderEngine(DAOFactory factory, Graph dependencies,
                             Node daoNode, InjectSPI spi) {
        this.factory = factory;
        this.dependencies = dependencies;
        this.spi = spi;

        rootNode = dependencies.getNode(null);
        daoPlaceholder = daoNode;
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized
     * engine from the given file.
     *
     * @param factory The DAO factory.
     * @param file    The file from which to load the recommender engine.
     * @throws IOException            if there's an error reading the file.
     * @throws ClassNotFoundException if the graph references a missing class.
     * @deprecated Use {@link #load(DAOFactory, File)} instead.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public LenskitRecommenderEngine(DAOFactory factory,
                                    File file) throws IOException, ClassNotFoundException {
        // TODO Make this delegate to load
        this.factory = factory;
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        try {
            spi = new ReflectionInjectSPI();
            dependencies = (Graph) in.readObject();
            rootNode = dependencies.getNode(null);
            daoPlaceholder = GraphtUtils.findDAONode(dependencies);
        } finally {
            in.close();
        }
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized
     * engine from the given file. The new engine will be identical to the old
     * except it will use the new DAOFactory. It is assumed that the file was
     * created by using {@link #write(OutputStream)}.
     *
     * @param factory The DAO factory.
     * @param file The file from which to load the engine.
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException If the configuration cannot be used.
     */
    public static LenskitRecommenderEngine load(DAOFactory factory, File file) throws IOException, RecommenderConfigurationException {
        FileInputStream input = new FileInputStream(file);
        try {
            return load(factory, input);
        } finally {
            input.close();
        }
    }

    /**
     * Create a new LenskitRecommenderEngine by reading a previously serialized
     * engine from the given input stream. The new engine will be identical to the old
     * except it will use the new DAOFactory. It is assumed that the file was
     * created by using {@link #write(OutputStream)}.
     *
     * @param factory The DAO factory.
     * @param input The stream from which to load the engine.
     * @return The loaded recommender engine.
     * @throws IOException If there is an error reading from the file.
     * @throws RecommenderConfigurationException If the configuration cannot be used.
     */
    public static LenskitRecommenderEngine load(DAOFactory factory, InputStream input) throws IOException, RecommenderConfigurationException {
        InjectSPI spi = new ReflectionInjectSPI();
        ObjectInputStream in = new ObjectInputStream(input);
        try {
            Graph dependencies = (Graph) in.readObject();
            Node daoNode = GraphtUtils.findDAONode(dependencies);
            return new LenskitRecommenderEngine(factory, dependencies, daoNode, spi);
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
     * @see #load(DAOFactory, InputStream)
     */
    public void write(@Nonnull OutputStream stream) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(stream);
        try {
            out.writeObject(dependencies);
        } finally {
            out.close();
        }
    }

    @Override
    public LenskitRecommender open() {
        if (factory == null) {
            throw new IllegalStateException("No DAO creator supplied");
        }
        DataAccessObject dao = factory.create();
        try {
            return open(dao, true);
        } catch (RuntimeException e) {
            dao.close();
            throw e;
        }
    }

    /**
     * Open a recommender with a specific data connection. The client code must
     * close the recommender when it is finished with it.
     *
     * @param dao         The DAO to connect the recommender to.
     * @param shouldClose If {@code true}, then the recommender should close the
     *                    DAO when it is closed.
     * @return A recommender ready for use and backed by {@var dao}.
     */
    public LenskitRecommender open(@Nonnull DataAccessObject dao, boolean shouldClose) {
        Preconditions.checkNotNull(dao, "Cannot open with null DAO");
        // Set up a session graph with the DAO node
        Graph sgraph = dependencies.clone();
        Node daoNode = new Node(new CachedSatisfaction(spi.satisfy(dao), CachePolicy.NO_PREFERENCE));
        if (daoPlaceholder != null) {
            sgraph.replaceNode(daoPlaceholder, daoNode);
        }
        Injector inj = new StaticInjector(spi, sgraph, rootNode);
        return new LenskitRecommender(inj, dao, shouldClose);
    }

    /**
     * Get the dependency graph of the recommender engine.
     *
     * @return The dependency graph.
     */
    Graph getDependencies() {
        return dependencies;
    }
}
