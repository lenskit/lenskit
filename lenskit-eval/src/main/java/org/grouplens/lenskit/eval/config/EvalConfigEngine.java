/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import com.google.common.base.Preconditions;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.grouplens.lenskit.eval.Command;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.*;

/**
 * Load and process configuration files. Also provides helper methods used by the
 * configuration scripts to locate & invoke methods.
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
public class EvalConfigEngine {
    private static Logger logger = LoggerFactory.getLogger(EvalConfigEngine.class);
    private static final String METHOD_PATH = "META-INF/lenskit-eval/methods/";

    protected ClassLoader classLoader;
    protected GroovyShell shell;
    protected EvalScriptConfig config;

    @SuppressWarnings("rawtypes")
    private final Map<Class, Class> commands = new HashMap<Class, Class>();

    public EvalConfigEngine() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public EvalConfigEngine(ClassLoader loader) {
        this(Thread.currentThread().getContextClassLoader(),
             new Properties(System.getProperties()));
    }

    public EvalConfigEngine(ClassLoader loader, Properties configProperties) {
        CompilerConfiguration compConfig = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        config = new EvalScriptConfig(configProperties);

        compConfig.setScriptBaseClass("org.grouplens.lenskit.eval.config.EvalConfigScript");

        ImportCustomizer imports = new ImportCustomizer();
        imports.addStarImports("org.grouplens.lenskit",
                               "org.grouplens.lenskit.params",
                               "org.grouplens.lenskit.baseline",
                               "org.grouplens.lenskit.norm",
                               "org.grouplens.lenskit.eval.metrics.predict",
                               "org.grouplens.lenskit.eval.metrics.recommend");
        compConfig.addCompilationCustomizers(imports);
        shell = new GroovyShell(loader, new Binding(), compConfig);
        classLoader = loader;

        loadCommands();
    }

    /**
     * Load a script from a file.
     *
     * @param file The file to read.
     * @return The script as parsed and compiled by Groovy.
     * @throws IOException if the file cannot be read.
     */
    protected EvalConfigScript loadScript(File file) throws IOException {
        EvalConfigScript script = (EvalConfigScript) shell.parse(file);
        script.setEngine(this);
        script.setConfig(config);
        return script;
    }

    /**
     * Load a script from a reader.
     *
     * @param in The reader to read.
     * @return The script as parsed and compiled by Groovy.
     */
    protected EvalConfigScript loadScript(Reader in) {
        EvalConfigScript script = (EvalConfigScript) shell.parse(in);
        script.setEngine(this);
        script.setConfig(config);
        return script;
    }

    /**
     * Run an evaluation config script and get the evaluations it produces.
     *
     * @param script The script to run (as loaded by Groovy)
     * @return A list of evaluations produced by {@code script}.
     * @throws CommandException if the script is invalid or produces an error.
     */
    protected
    @Nullable
    Object runScript(EvalConfigScript script, String[] args) throws CommandException {
        script.setBinding(new Binding(args));
        Object result = null;
        try {
            result = script.run();
        } catch (RuntimeException e) {
            throw new CommandException("error running configuration script", e);
        } catch (LinkageError e) {
            throw new CommandException("error running configuration script", e);
        }
        return result;
    }

    /**
     * Load a set of evaluations from a script file.
     *
     * @param file A Groovy script to configure the evaluator.
     * @return A list of evaluations to run.
     * @throws CommandException if there is a configuration error
     * @throws IOException      if there is an error reading the file
     */
    public
    @Nullable
    Object execute(File file) throws CommandException, IOException {
        logger.debug("loading script file {}", file);
        return execute(file, new String[]{});
    }

    /**
     * Load a set of evaluations from a script file.
     *
     * @param file A Groovy script to configure the evaluator.
     * @param args The command line arguments for the script.
     * @return A list of evaluations to run.
     * @throws CommandException if there is a configuration error
     * @throws IOException      if there is an error reading the file
     */
    public
    @Nullable
    Object execute(File file, String[] args) throws CommandException, IOException {
        logger.debug("loading script file {}", file);
        return runScript(loadScript(file), args);
    }

    /**
     * Load a set of evaluations from an input stream.
     *
     * @param in The input stream
     * @return A list of evaluations
     * @throws CommandException if there is a configuration error
     */
    public
    @Nullable
    Object execute(Reader in) throws CommandException {
        return execute(in, new String[]{});
    }

    /**
     * Load a set of evaluations from an input stream.
     *
     * @param in   The input stream
     * @param args The command line arguments for the script.
     * @return A list of evaluations
     * @throws CommandException if there is a configuration error
     */
    public
    @Nullable
    Object execute(Reader in, String[] args) throws CommandException {
        return runScript(loadScript(in), args);
    }

    /**
     * Find a command with a particular name if it exists.
     *
     * @param name The name of the command
     * @return The command factory or {@code null} if no such factory exists.
     */
    @CheckForNull
    @Nullable
    public Class<? extends Command> getCommand(@Nonnull String name) {
        String path = METHOD_PATH + name + ".properties";
        logger.debug("loading method {} from {}", name, path);
        InputStream istr = classLoader.getResourceAsStream(path);
        if (istr == null) {
            logger.debug("path {} not found", path);
            return null;
        }

        try {
            Properties props = new Properties();
            props.load(istr);
            Object pv = props.get("command").toString();
            String className = pv == null ? null : pv.toString();
            if (className == null) {
                return null;
            }

            return Class.forName(className).asSubclass(Command.class);
        } catch (IOException e) {
            throw new RuntimeException("error reading method " + name, e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("cannot find command class", e);
        } finally {
            LKFileUtils.close(istr);
        }
    }

    /**
     * Get a command for a type. It consults registered commands and looks for the
     * {@link BuilderCommand} annotation.
     *
     * @param type A type that needs to be built.
     * @return A command class to build {@code type}, or {@code null} if none can be found.
     * @see #registerCommand
     */
    @SuppressWarnings("unchecked")
    public <T> Class<? extends Command> getCommandForType(Class<T> type) {
        @SuppressWarnings("rawtypes")
        Class command = commands.get(type);
        if (command == null) {
            BuilderCommand annot = type.getAnnotation(BuilderCommand.class);
            if (annot != null) {
                command = annot.value();
            }
        }
        return command;
    }

    /**
     * Register a command class for a type. Used to allow commands to be found for types where
     * the type cannot be augmented with the {@code DefaultBuilder} annotation.
     *
     * @param type    The type to build.
     * @param command A class that can build instances of {@code type}.
     * @param <T>     The type to build (type parameter).
     */
    public <T> void registerCommand(Class<T> type, Class<? extends Command> command) {
        Preconditions.checkNotNull(type, "type cannot be null");
        Preconditions.checkNotNull(command, "command cannot be null");
        commands.put(type, command);
    }

    /**
     * Register a default set of commands.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void loadCommands() {
        Properties props = new Properties();
        try {
            for (URL url : Collections.list(classLoader.getResources("META-INF/lenskit-eval/builders.properties"))) {
                InputStream istr = url.openStream();
                try {
                    props.load(istr);
                } finally {
                    LKFileUtils.close(istr);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<Object, Object> prop : props.entrySet()) {
            String name = prop.getKey().toString();
            String command = prop.getValue().toString();
            Class cls;
            try {
                cls = Class.forName(name);
            } catch (ClassNotFoundException e) {
                logger.warn("command registered for nonexistent class {}", name);
                continue;
            }
            Class cmd;
            try {
                cmd = Class.forName(command).asSubclass(Command.class);
            } catch (ClassNotFoundException e) {
                logger.error("command class {} not found", command);
                continue;
            } catch (ClassCastException e) {
                logger.error("class {} is not a command", command);
                continue;
            }
            logger.debug("registering {} as command for {}", command, cls);
            registerCommand(cls, cmd);
        }
    }
}
