/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
import org.grouplens.lenskit.core.EngineValidationMode;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.WillClose;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Load a pre-built recommender engine from a file.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitRecommenderEngineLoader {
    private static final Logger logger = LoggerFactory.getLogger(LenskitRecommenderEngineLoader.class);
    private org.grouplens.lenskit.core.LenskitRecommenderEngineLoader delegate =
            new org.grouplens.lenskit.core.LenskitRecommenderEngineLoader();

    /**
     * Get the configured class loader.
     * @return The class loader that will be used when loading the engine.
     */
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }

    /**
     * Set the class loader to use when reading the engine.
     * @param classLoader The class loader to use.
     * @return The loader (for chaining).
     */
    public LenskitRecommenderEngineLoader setClassLoader(ClassLoader classLoader) {
        delegate.setClassLoader(classLoader);
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
        delegate.addConfiguration(config);
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
        delegate.setValidationMode(mode);
        return this;
    }

    /**
     * Set the compression mode to use.  The default is {@link CompressionMode#AUTO}.
     * @param comp The compression mode.
     */
    public void setCompressionMode(CompressionMode comp) {
        delegate.setCompressionMode(comp);
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
        return new LenskitRecommenderEngine(delegate.load(stream));
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
        return new LenskitRecommenderEngine(delegate.load(file));
    }
}
