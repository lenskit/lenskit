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
package org.grouplens.lenskit.config;

import com.google.common.base.Preconditions;
import groovy.lang.*;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.grouplens.grapht.util.ClassLoaders;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.grouplens.lenskit.util.ClassDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Load LensKit configurations using the configuration DSL.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
    private final ClassLoader classLoader;
    private final GroovyShell shell;
    private final Binding binding;
    private final ClassDirectory directory;
    private int scriptNumber;

    /**
     * Construct a new configuration loader. It uses the current thread's class loader.
     */
    public ConfigurationLoader() {
        this(ClassLoaders.inferDefault(ConfigurationLoader.class));
    }

    /**
     * Construct a new configuration loader.
     * @param loader The class loader to use.
     */
    public ConfigurationLoader(ClassLoader loader) {
        classLoader = loader;
        binding = new Binding();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(LenskitConfigScript.class.getName());
        ImportCustomizer imports = new ImportCustomizer();
        imports.addStarImports("org.grouplens.lenskit");
        config.addCompilationCustomizers(imports);
        shell = new GroovyShell(loader, binding, config);
        directory = ClassDirectory.forClassLoader(loader);
    }

    public ClassDirectory getDirectory() {
        return directory;
    }

    /**
     * Load a LensKit configuration script.  This method is for internal use that needs to override how configuration
     * scripts are loaded.
     *
     * @param source The source
     * @return the configuration script.
     * @throws RecommenderConfigurationException
     */
    public LenskitConfigScript loadScript(GroovyCodeSource source) throws RecommenderConfigurationException {
        logger.info("loading script from {}", source.getName());
        LenskitConfigScript script;
        try {
            script = (LenskitConfigScript) shell.parse(source);
        } catch (GroovyRuntimeException e) {
            throw new RecommenderConfigurationException("Error loading Groovy script", e);
        }
        script.setDelegate(new LenskitConfigDSL(this));;
        return script;
    }

    /**
     * Load a configuration from a file.
     * @param file The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(@Nonnull File file) throws IOException, RecommenderConfigurationException {
        Preconditions.checkNotNull(file, "Configuration file");
        return loadScript(file).configure();
    }

    /**
     * Load a configuration from a URL.
     * @param url The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(@Nonnull URL url) throws IOException, RecommenderConfigurationException {
        Preconditions.checkNotNull(url, "Configuration URL");
        return loadScript(url).configure();
    }

    /**
     * Load a configuration from a script source.
     * @param source The configuration script to load.
     * @return The resulting LensKit configuration.
     * @deprecated Loading from Groovy sources as strings is confusing.
     */
    @Deprecated
    public LenskitConfiguration load(@Nonnull String source) throws RecommenderConfigurationException {
        Preconditions.checkNotNull(source, "Configuration source text");
        return loadScript(source).configure();
    }

    /**
     * Load a configuration script from a file.
     * @param file The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfigScript loadScript(@Nonnull File file) throws IOException, RecommenderConfigurationException {
        Preconditions.checkNotNull(file, "Configuration file");
        return loadScript(new GroovyCodeSource(file));
    }

    /**
     * Load a configuration script from a URL.
     * @param url The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfigScript loadScript(@Nonnull URL url) throws IOException, RecommenderConfigurationException {
        Preconditions.checkNotNull(url, "Configuration URL");
        return loadScript(new GroovyCodeSource(url));
    }

    /**
     * Load a configuration script from a script source.
     * @param source The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfigScript loadScript(@Nonnull String source) throws RecommenderConfigurationException {
        Preconditions.checkNotNull(source, "Configuration source text");
        return loadScript(new GroovyCodeSource(source, "LKConfig" + (++scriptNumber),
                                               GroovyShell.DEFAULT_CODE_BASE));
    }

    /**
     * Load a configuration from a closure. The class loader is not really consulted in this case.
     * @param block The block to evaluate. This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link Closure#DELEGATE_FIRST} resolution strategy.
     * @return The resulting LensKit configuration.
     * @deprecated Use {@link ConfigHelpers#load(groovy.lang.Closure)} instead.
     */
    @Deprecated
    public LenskitConfiguration load(@Nonnull Closure<?> block) throws RecommenderConfigurationException {
        return ConfigHelpers.load(block);
    }
}
