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

import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Load a pre-built recommender engine from a file.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitRecommenderEngineLoader {
    private static final Logger logger = LoggerFactory.getLogger(LenskitRecommenderEngineLoader.class);
    private ClassLoader classLoader;

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

    public LenskitRecommenderEngine load(InputStream stream) throws IOException, RecommenderConfigurationException {
        logger.debug("using classloader {}", classLoader);
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
                DAGNode<CachedSatisfaction, DesireChain> dependencies = (DAGNode) in.readObject();
                return new LenskitRecommenderEngine(dependencies, spi);
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
