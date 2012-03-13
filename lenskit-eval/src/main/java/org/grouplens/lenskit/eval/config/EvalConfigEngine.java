/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval.config;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.apache.commons.lang3.builder.Builder;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.Evaluation;
import org.grouplens.lenskit.eval.EvaluatorConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * Load and process configuration files. Also provides helper methods used by the
 * configuration scripts to locate & invoke methods.
 * @author Michael Ekstrand
 * @since 0.10
 */
public class EvalConfigEngine {
    private static Logger logger = LoggerFactory.getLogger(EvalConfigEngine.class);

    protected ClassLoader classLoader;
    protected GroovyShell shell;

    private Map<String,BuilderFactory<?>> factories = null;
    private Map<Class, Class> builders = new HashMap<Class, Class>();

    public EvalConfigEngine() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public EvalConfigEngine(ClassLoader loader) {
        CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        config.setScriptBaseClass("org.grouplens.lenskit.eval.config.EvalConfigScript");

        ImportCustomizer imports = new ImportCustomizer();
        imports.addStarImports("org.grouplens.lenskit",
                               "org.grouplens.lenskit.params",
                               "org.grouplens.lenskit.baseline",
                               "org.grouplens.lenskit.norm",
                               "org.grouplens.lenskit.eval.metrics.predict");
        config.addCompilationCustomizers(imports);

        shell = new GroovyShell(loader, new Binding(), config);
        classLoader = loader;

        registerDefaultBuilders();
    }

    /**
     * Load a script from a file.
     * @param file The file to read.
     * @return The script as parsed and compiled by Groovy.
     * @throws IOException if the file cannot be read.
     */
    protected EvalConfigScript loadScript(File file) throws IOException {
        EvalConfigScript script =  (EvalConfigScript) shell.parse(file);
        script.setEngine(this);
        return script;
    }

    /**
     * Load a script from a reader.
     * @param in The reader to read.
     * @return The script as parsed and compiled by Groovy.
     */
    protected EvalConfigScript loadScript(Reader in) {
        EvalConfigScript script = (EvalConfigScript) shell.parse(in);
        script.setEngine(this);
        return script;
    }

    /**
     * Run an evaluation config script and get the evaluations it produces.
     * @param script The script to run (as loaded by Groovy)
     * @return A list of evaluations produced by {@code script}.
     * @throws EvaluatorConfigurationException if the script is invalid or produces an error.
     */
    protected List<Evaluation> runScript(EvalConfigScript script) throws EvaluatorConfigurationException {
        List<Evaluation> evals = new LinkedList<Evaluation>();
        try {
            Object obj = script.run();
            if (obj instanceof Evaluation) {
                evals.add((Evaluation) obj);
            } else {
                throw new EvaluatorConfigurationException("configuration script did not yield an evaluation");
            }
        } catch (RuntimeException e) {
            throw new EvaluatorConfigurationException("error running configuration script", e);
        }
        return evals;
    }

    /**
     * Load a set of evaluations from a script file.
     * @param file A Groovy script to configure the evaluator.
     * @return A list of evaluations to run.
     * @throws EvaluatorConfigurationException if there is a configuration error
     * @throws IOException if there is an error reading the file
     */
    public List<Evaluation> load(File file) throws EvaluatorConfigurationException, IOException {
        logger.debug("loading script file {}", file);
        return runScript(loadScript(file));
    }

    /**
     * Load a set of evaluations from an input stream.
     * @param in The input stream
     * @return A list of evaluations
     * @throws EvaluatorConfigurationException if there is a configuration error
     */
    public List<Evaluation> load(Reader in) throws EvaluatorConfigurationException {
        return runScript(loadScript(in));
    }

    /**
     * Get the map of names to builder factories.
     * @return The mapping of builder factory names.
     */
    synchronized Map<String,BuilderFactory<?>> getFactories() {
        if (factories == null) {
            ServiceLoader<BuilderFactory> loader = ServiceLoader.load(BuilderFactory.class, classLoader);
            factories = new HashMap<String,BuilderFactory<?>>();
            for (BuilderFactory f: loader) {
                logger.debug("Found factory {}", f.getName());
                factories.put(f.getName(), f);
            }
        }
        return factories;
    }

    /**
     * Find a builder factory with a particular name if it exists.
     * @param name The name of the builder
     * @return The builder factory or {@code null} if no such factory exists.
     */
    @CheckForNull
    public BuilderFactory<?> getBuilderFactory(@Nonnull String name) {
        return getFactories().get(name);
    }

    /**
     * Get a builder for a type. It consults registered builders and looks for the
     * {@link DefaultBuilder} annotation.
     * @param type A type that needs to be built.
     * @return A builder class to build {@code type}, or {@code null} if none can be found.
     * @see #registerBuilder(Class, Class)
     */
    @SuppressWarnings("unchecked")
    public <T> Class<? extends Builder<? extends T>> getBuilderForType(Class<T> type) {
        Class builder = builders.get(type);
        if (builder == null) {
            DefaultBuilder annot = type.getAnnotation(DefaultBuilder.class);
            if (annot != null) {
                builder = annot.value();
            }
        }
        return builder;
    }

    /**
     * Register a builder class for a type. Used to allow builders to be found for types where
     * the type cannot be augmented with the {@code DefaultBuilder} annotation.
     * @param type The type to build.
     * @param builder A class that can build instances of {@code type}.
     * @param <T> The type to build (type parameter).
     */
    public <T> void registerBuilder(Class<T> type, Class<? extends Builder<? extends T>> builder) {
        builders.put(type, builder);
    }

    /**
     * Register a default set of builders.
     */
    protected void registerDefaultBuilders() {
        registerBuilder(PreferenceDomain.class, PreferenceDomainBuilder.class);
    }
}
