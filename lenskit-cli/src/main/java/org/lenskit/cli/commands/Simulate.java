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
package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import org.lenskit.config.ConfigurationLoader;
import org.lenskit.eval.temporal.TemporalEvaluator;
import org.lenskit.LenskitConfiguration;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.cli.Command;
import org.lenskit.cli.util.ScriptEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class Simulate implements Command {
    private final Logger logger = LoggerFactory.getLogger(Simulate.class);

    public void configureArguments(ArgumentParser parser) {
        parser.description("Simulates a recommender over time");
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("-i", "--input-file")
              .type(File.class)
              .metavar("FILE")
              .setDefault("ratings.pack")
              .help("Packed Rating File");
        parser.addArgument("-o", "--output-file")
              .type(File.class)
              .metavar("FILE")
              .setDefault("ratings.pack")
              .help("write predicted score to FILE");
        parser.addArgument("-r", "--rebuild-period")
              .type(Long.class)
              .metavar("SECONDS")
              .help("Rebuild Period for next build");
        parser.addArgument("config")
              .type(File.class)
              .metavar("CONFIG")
              .help("load algorithm configuration from CONFIG");
    }

    @Override
    public String getName() {
        return "simulate";
    }

    @Override
    public String getHelp() {
        return "simulate";
    }

    @Override
    public void execute(Namespace opts) throws IOException, RecommenderBuildException {

        Context ctx = new Context(opts);
        TemporalEvaluator tempEval = new TemporalEvaluator();

        Stopwatch timer = Stopwatch.createStarted();

        Long rebuildPeriod = ctx.getRebuildPeriod();
        tempEval.setDataSource(ctx.getInputFile());
        tempEval.setPredictOutputFile(ctx.getOutputFile());
        tempEval.setRebuildPeriod(rebuildPeriod);

        ConfigurationLoader loader = new ConfigurationLoader();
        LenskitConfiguration config = loader.load(ctx.getConfigFile());
        tempEval.setAlgorithm(config.toString(), config);

        tempEval.execute();
        timer.stop();
        logger.info("evaluator executed  in {}", timer);
        logger.info("written predicted score to {}", ctx.getOutputFile());
        if (rebuildPeriod != null) {
            logger.info("Rebuild period set to {}", ctx.getRebuildPeriod());
        }
    }

    private static class Context {
        private final Namespace options;

        public Context(Namespace opts) {
            options = opts;
        }

        public File getInputFile() {
            return options.get("input_file");
        }

        public File getOutputFile() {
            return options.get("output_file");
        }

        public File getConfigFile() {
            return options.get("config");
        }

        public Long getRebuildPeriod() {
            return options.get("rebuild_period");
        }
    }
}
