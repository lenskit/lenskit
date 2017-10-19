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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.util.ClassLoaderContext;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.util.io.CompressionMode;
import org.lenskit.util.io.CustomClassLoaderObjectInputStream;
import org.lenskit.util.io.LKFileUtils;
import org.lenskit.inject.GraphtUtils;
import org.lenskit.inject.RecommenderGraphBuilder;
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
    public LenskitRecommenderEngineLoader setCompressionMode(CompressionMode comp) {
        compressionMode = comp;
        return this;
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
        try (FileInputStream input = new FileInputStream(file)) {
            CompressionMode effComp = compressionMode.getEffectiveCompressionMode(file.getName());
            logger.info("using {} compression", effComp);
            return loadInternal(effComp.wrapInput(input));
        }
    }

    /**
     * Read a graph from an object input stream. Broken out to localize the necessary warning
     * suppression.
     *
     * @param in The input stream.
     * @return the loaded graph.
     * @throws IOException If there is an I/O error.
     * @throws ClassNotFoundException If there is a class resolution error.
     * @see ObjectInput#readObject()
     */
    @SuppressWarnings("unchecked")
    private DAGNode<Component, Dependency> readGraph(ObjectInput in) throws IOException, ClassNotFoundException {
        return (DAGNode) in.readObject();
    }

    /**
     * Load a recommender engine from an input stream.  It transparently decompresses the stream
     * and handles the classloader nastiness.
     *
     * @param stream The input stream.
     * @return The recommender engine.
     * @throws IOException If there is an I/O error reading the engine.
     * @throws RecommenderConfigurationException If there is a configuration error (including an
     * invalid class reference in the object stream).
     */
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
                graph = readGraph(in);
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
            } catch (ResolutionException e) {
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
