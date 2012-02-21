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
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.grouplens.lenskit.eval.Evaluation;
import org.grouplens.lenskit.eval.EvaluatorConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Load and process configuration files.
 * @author Michael Ekstrand
 * @since 0.10
 */
public class EvalConfigEngine {
    private static Logger logger = LoggerFactory.getLogger(EvalConfigEngine.class);

    protected ClassLoader classLoader;
    protected GroovyShell shell;

    public EvalConfigEngine() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public EvalConfigEngine(ClassLoader loader) {
        CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        config.setScriptBaseClass("org.grouplens.lenskit.eval.config.EvalConfigScript");
        shell = new GroovyShell(loader, new Binding(), config);
        classLoader = loader;
    }

    protected EvalConfigScript loadScript(File file) throws IOException {
        EvalConfigScript script =  (EvalConfigScript) shell.parse(file);
        script.setEngine(this);
        return script;
    }

    protected EvalConfigScript loadScript(InputStream in) {
        EvalConfigScript script = (EvalConfigScript) shell.parse(in);
        script.setEngine(this);
        return script;
    }

    protected List<Evaluation> runScript(EvalConfigScript script) throws EvaluatorConfigurationException {
        try {
            script.run();
        } catch (RuntimeException e) {
            throw new EvaluatorConfigurationException("error running configuration script", e);
        }
        return script.getEvaluations();
    }

    public List<Evaluation> load(File file) throws EvaluatorConfigurationException, IOException {
        logger.debug("loading script file {}", file);
        return runScript(loadScript(file));
    }

    public List<Evaluation> load(InputStream in) throws EvaluatorConfigurationException {
        return runScript(loadScript(in));
    }

    public Map<String,BuilderFactory> loadFactories() {
        ServiceLoader<BuilderFactory> loader = ServiceLoader.load(BuilderFactory.class, classLoader);
        Map<String,BuilderFactory> factories = new HashMap<String,BuilderFactory>();
        for (BuilderFactory f: loader) {
            logger.debug("Found factory {}", f.getName());
            factories.put(f.getName(), f);
        }
        return factories;
    }
}
