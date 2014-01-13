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
package org.grouplens.lenskit.cli;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.util.Providers;
import org.grouplens.lenskit.config.ConfigurationLoader;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.graph.GraphDumper;
import org.grouplens.lenskit.inject.RecommenderGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Command that draws a diagram of a recommender configuration.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@CommandSpec(name = "graph", help = "diagram a recommender configuration")
public class Graph implements Command {
    private final Logger logger = LoggerFactory.getLogger(Graph.class);
    private final Namespace options;
    private final ScriptEnvironment environment;

    public Graph(Namespace opts) {
        options = opts;
        environment = new ScriptEnvironment(opts);
    }

    public List<File> getConfigFiles() {
        return options.get("config");
    }

    public File getOutputFile() {
        return options.get("output_file");
    }

    private LenskitConfiguration makeDataConfig() {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).toProvider(Providers.<EventDAO>of(null, EventDAO.class));
        String dspec = options.getString("domain");
        if (dspec != null) {
            PreferenceDomain domain = PreferenceDomain.fromString(dspec);
            config.bind(PreferenceDomain.class).to(domain);
        }
        return config;
    }

    private DAGNode<CachedSatisfaction,DesireChain> makeGraph() throws IOException, RecommenderConfigurationException {
        ConfigurationLoader loader = new ConfigurationLoader(environment.getClassLoader());
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.addConfiguration(makeDataConfig());
        for (File file: getConfigFiles()) {
            logger.info("loading configuration from {}", file);
            LenskitConfiguration config = loader.load(file);
            rgb.addConfiguration(config);
        }

        try {
            return rgb.buildGraph();
        } catch (SolverException e) {
            throw new RecommenderConfigurationException("Cannot configure recommender", e);
        }
    }

    @Override
    public void execute() throws IOException, RecommenderConfigurationException {
        DAGNode<CachedSatisfaction, DesireChain> graph = makeGraph();
        File output = getOutputFile();
        logger.info("writing graph to {}", output);
        GraphDumper.renderGraph(graph, output);
    }

    public static void configureArguments(ArgumentParser parser) {
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("-o", "--output-file")
              .type(File.class)
              .metavar("FILE")
              .setDefault(new File("recommender.dot"))
              .help("write recommender diagram to FILE");
        parser.addArgument("--domain")
              .metavar("DOMAIN")
              .help("specify preference domain");
        parser.addArgument("config")
              .type(File.class)
              .metavar("CONFIG")
              .nargs("*")
              .help("load algorithm configuration from file CONFIG");
    }
}
