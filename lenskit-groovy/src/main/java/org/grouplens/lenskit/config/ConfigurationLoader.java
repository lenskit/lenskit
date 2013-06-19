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
package org.grouplens.lenskit.config;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.grouplens.lenskit.core.LenskitConfiguration;

import java.io.File;

/**
 * Load LensKit configurations using the configuration DSL.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConfigurationLoader {
    private final ClassLoader classLoader;
    private final GroovyShell shell;
    private final Binding binding;

    /**
     * Construct a new configuration loader. It uses the current thread's class loader.
     * @review Is this the classloader we should use?
     */
    public ConfigurationLoader() {
        this(Thread.currentThread().getContextClassLoader());
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
    }

    /**
     * Load a configuration from a file.
     * @param file The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(File file) {
        return null;
    }

    /**
     * Load a configuration from a script source.
     * @param source The configuration script to load.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(String source) {
        LenskitConfigScript script = (LenskitConfigScript) shell.parse(source);
        script.run();
        return script.getConfig();
    }

    /**
     * Load a configuration from a closure. The class loader is not really consulted in this case.
     * @param block The block to evaluate. This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link Closure#DELEGATE_FIRST} resolution strategy.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(Closure<?> block) {
        LenskitConfiguration config = new LenskitConfiguration();
        BindingDSL delegate = new LenskitConfigDSL(config);
        block.setDelegate(delegate);
        block.setResolveStrategy(Closure.DELEGATE_FIRST);
        block.call();
        return config;
    }
}
