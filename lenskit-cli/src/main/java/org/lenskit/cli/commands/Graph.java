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
package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.graph.DAGNode;
import org.lenskit.*;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.graph.GraphDumper;
import org.lenskit.inject.RecommenderGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.List;

/**
 * Command that draws a diagram of a recommender configuration.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class Graph implements Command {
    private final Logger logger = LoggerFactory.getLogger(Graph.class);

    @Override
    public String getName() {
        return "graph";
    }

    @Override
    public String getHelp() {
        return "diagram a recommender configuration";
    }

    private LenskitConfiguration makeDataConfig(Context ctx) {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(DataAccessObject.class).toProvider(new DAOProvider());
        String dspec = ctx.options.getString("domain");
        if (dspec != null) {
            PreferenceDomain domain = PreferenceDomain.fromString(dspec);
            config.bind(PreferenceDomain.class).to(domain);
        }
        return config;
    }

    /**
     * Load a configuration graph from a recommender model.
     * @param file The model file.
     * @return The recommender graph.
     * @throws IOException if there is an error loading the model.
     * @throws RecommenderConfigurationException if the model fails to configure.
     */
    private DAGNode<Component, Dependency> loadModel(Context ctx, File file) throws IOException, RecommenderConfigurationException {
        logger.info("loading model from {}", file);
        LenskitRecommenderEngineLoader loader = LenskitRecommenderEngine.newLoader();
        loader.setValidationMode(EngineValidationMode.DEFERRED)
              .addConfiguration(makeDataConfig(ctx));
        for (LenskitConfiguration config: ctx.environment.loadConfigurations(ctx.getConfigFiles())) {
            loader.addConfiguration(config);
        }
        LenskitRecommenderEngine engine = loader.load(file);
        return engine.getGraph();
    }

    /**
     * Build a configured recommender graph from the specified configurations.
     * @return The configuration graph.
     * @throws IOException if there is an error loading the configurations
     * @throws RecommenderConfigurationException if there is an error building the configurations
     */
    private DAGNode<Component,Dependency> makeNewGraph(Context ctx) throws IOException, RecommenderConfigurationException {
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.addConfiguration(makeDataConfig(ctx));
        for (LenskitConfiguration config: ctx.environment.loadConfigurations(ctx.getConfigFiles())) {
            rgb.addConfiguration(config);
        }

        try {
            return rgb.buildGraph();
        } catch (ResolutionException e) {
            throw new RecommenderConfigurationException("Cannot configure recommender", e);
        }
    }

    @Override
    public void execute(Namespace opts) throws LenskitCommandException {
        Context ctx = new Context(opts);
        File modelFile = opts.get("model_file");
        DAGNode<Component, Dependency> graph;
        if (modelFile != null) {
            try {
                graph = loadModel(ctx, modelFile);
            } catch (IOException e) {
                throw new LenskitCommandException("failed to load model", e);
            }
        } else {
            try {
                graph = makeNewGraph(ctx);
            } catch (IOException e) {
                throw new LenskitCommandException("failed to instantiate graph");
            }
        }
        File output = ctx.getOutputFile();
        try {
            switch (ctx.getOutputType()) {
            case dot:
                writeDotFile(graph, output);
                break;
            case svg:
                writeSvgFile(graph, output);
                break;
            }
        } catch (IOException e) {
            throw new LenskitCommandException("error writing graph output", e);
        }
    }

    private void writeDotFile(DAGNode<Component, Dependency> graph, File outFile) throws IOException {
        try (Writer writer = new FileWriter(outFile)) {
            logger.info("writing graph to {}", outFile);
            GraphDumper.renderGraph(graph, writer);
        }
    }

    private void writeSvgFile(DAGNode<Component, Dependency> graph, File outFile) throws IOException, LenskitCommandException {
        StringWriter sw = new StringWriter();
        logger.info("writing graph to memory");
        GraphDumper.renderGraph(graph, sw);
        String dotSrc = sw.toString();

        logger.debug("setting up script engine");
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByMimeType("text/javascript");
        try (InputStream istr = Graph.class.getResourceAsStream("/META-INF/resources/webjars/viz.js/1.5.1/viz.js");
             Reader rdr = new InputStreamReader(istr)) {
            logger.debug("loading Viz.js");
            engine.put(ScriptEngine.FILENAME, "viz.js");
            engine.eval(rdr);
        } catch (ScriptException e) {
            logger.error("error loading Viz.js", e);
            throw new LenskitCommandException("Could not load Viz.js", e);
        }
        engine.put("dotSrc", dotSrc);
        engine.put("outFile", outFile);
        try (InputStream istr = Graph.class.getResourceAsStream("render-graph.js");
             Reader rdr = new InputStreamReader(istr)) {
            logger.info("rendering graph to {}", outFile);
            engine.put(ScriptEngine.FILENAME, "render-graph.js");
            engine.eval(rdr);
        } catch (ScriptException e) {
            logger.error("error evaluating render script", e);
            throw new LenskitCommandException("could not evaluate SVG renderer", e);
        }
    }

    public void configureArguments(ArgumentParser parser) {
        parser.description("Generates a visualization of a recommender configuration. " +
                           "This visualization is intended to be viewed with GraphViz.");
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("-o", "--output-file")
              .type(File.class)
              .metavar("FILE")
              .setDefault(new File("recommender.dot"))
              .help("write recommender diagram to FILE");
        parser.addArgument("-t", "--output-type")
              .type(OutputType.class)
              .metavar("TYPE")
              .setDefault(OutputType.dot)
              .help("specify output format");
        parser.addArgument("--domain")
              .metavar("DOMAIN")
              .help("specify preference domain");
        parser.addArgument("--model-file")
              .metavar("FILE")
              .type(File.class)
              .help("load saved model from FILE");
        parser.addArgument("config")
              .type(File.class)
              .metavar("CONFIG")
              .nargs("*")
              .help("load algorithm configuration from file CONFIG");
    }

    private static class DAOProvider implements Provider<DataAccessObject> {
        @Override
        public DataAccessObject get() {
            return null;
        }

        @Override
        public String toString() {
            return "Data";
        }
    }

    private static class Context {
        private final Namespace options;
        private final ScriptEnvironment environment;

        Context(Namespace opts) {
            options = opts;
            environment = new ScriptEnvironment(opts);
        }

        List<File> getConfigFiles() {
            return options.get("config");
        }

        File getOutputFile() {
            return options.get("output_file");
        }

        OutputType getOutputType() {
            return options.get("output_type");
        }
    }

    private enum OutputType {
        dot, svg
    }
}
