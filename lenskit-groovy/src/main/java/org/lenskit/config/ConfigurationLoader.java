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
package org.lenskit.config;

import com.google.common.base.Preconditions;
import groovy.lang.*;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;
import org.lenskit.util.ClassDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
        this(null);
    }

    /**
     * Construct a new configuration loader.
     * @param loader The class loader to use.
     */
    public ConfigurationLoader(ClassLoader loader) {
        classLoader = loader != null ? loader : ClassLoaders.inferDefault(ConfigurationLoader.class);
        binding = new Binding();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(LenskitConfigScript.class.getName());
        ImportCustomizer imports = new ImportCustomizer();
        imports.addStarImports("org.lenskit.api");
        imports.addStarImports("org.lenskit.basic");
        config.addCompilationCustomizers(imports);
        shell = new GroovyShell(loader, binding, config);
        directory = ClassDirectory.forClassLoader(classLoader);
    }

    public ClassDirectory getDirectory() {
        return directory;
    }

    /**
     * Load a LensKit configuration script.  This method is for internal use that needs to override how configuration
     * scripts are loaded.
     *
     * @param source The source
     * @param base   The base URI for this configuration
     * @return the configuration script.
     * @throws RecommenderConfigurationException
     */
    public LenskitConfigScript loadScript(GroovyCodeSource source, URI base) throws RecommenderConfigurationException {
        logger.debug("loading script from {}", source.getName());
        LenskitConfigScript script;
        try {
            script = (LenskitConfigScript) shell.parse(source);
        } catch (GroovyRuntimeException e) {
            throw new RecommenderConfigurationException("Error loading Groovy script", e);
        }
        script.setDelegate(new LenskitConfigDSL(this, base));
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
        logger.info("loading recommender configuration from {}", file);
        return loadScript(new GroovyCodeSource(file), file.toURI());
    }

    /**
     * Load a configuration script from a URL.
     * @param url The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfigScript loadScript(@Nonnull URL url) throws IOException, RecommenderConfigurationException {
        Preconditions.checkNotNull(url, "Configuration URL");
        try {
            return loadScript(new GroovyCodeSource(url), url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI", e);
        }
    }

    /**
     * Load a configuration script from a script source.
     * @param source The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfigScript loadScript(@Nonnull String source) throws RecommenderConfigurationException {
        Preconditions.checkNotNull(source, "Configuration source text");
        return loadScript(new GroovyCodeSource(source, "LKConfig" + (++scriptNumber),
                                               GroovyShell.DEFAULT_CODE_BASE), null);
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
