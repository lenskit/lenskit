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
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.cli.Command;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.eval.temporal.TemporalEvaluator;
import org.lenskit.specs.SpecUtils;
import org.lenskit.specs.eval.AlgorithmSpec;
import org.lenskit.specs.eval.SimulateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


/**
 * Simulates a recommender algorithm over time.
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
              .setDefault("predictions.csv")
              .help("write predictions and errors to FILE");
        parser.addArgument("--extended-output")
              .type(File.class)
              .metavar("FILE")
              .setDefault("extended-output.txt")
              .help("write extended output as JSON lines in FILE");
        parser.addArgument("-n", "--list-size")
              .type(Integer.class)
              .metavar("INTEGER")
              .setDefault(10)
              .help("Length of recommendation lists");
        parser.addArgument("-r", "--rebuild-period")
              .type(Long.class)
              .setDefault(86400L)
              .metavar("SECONDS")
              .help("Rebuild Period for next build");
        parser.addArgument("--spec-file")
              .type(File.class)
              .metavar("SPEC")
              .help("load from spec file SPEC");
        parser.addArgument("config")
              .type(File.class)
              .metavar("CONFIG")
              .nargs("?")
              .help("load algorithm configuration from CONFIG");
    }

    @Override
    public String getName() {
        return "simulate";
    }

    @Override
    public String getHelp() {
        return "Simulate a recommender algorithm over time";
    }

    @Override
    public void execute(Namespace opts) throws IOException, RecommenderBuildException {

        Context ctx = new Context(opts);
        SimulateSpec spec;

        File specFile = opts.get("spec_file");
        if (specFile != null) {
            spec = SpecUtils.load(SimulateSpec.class, specFile.toPath());
        } else {
            spec = new SimulateSpec();

            spec.setListSize(ctx.getListSize());
            spec.setRebuildPeriod(ctx.getRebuildPeriod());

            spec.setInputFile(ctx.getInputFile().toPath());
            File out = ctx.getOutputFile();
            if (out != null) {
                spec.setOutputFile(out.toPath());
            }
            out = ctx.getExtendedOutputFile();
            if (out != null) {
                spec.setExtendedOutputFile(out.toPath());
            }

            AlgorithmSpec algo = new AlgorithmSpec();
            File cfg = ctx.getConfigFile();
            algo.setName(cfg.getName());
            algo.setConfigFile(cfg.toPath());
        }

        TemporalEvaluator eval = new TemporalEvaluator(spec);
        Stopwatch timer = Stopwatch.createStarted();
        logger.info("beginning temporal evaluator");
        eval.execute();
        timer.stop();
        logger.info("evaluator executed  in {}", timer);
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

        public File getExtendedOutputFile() {
            return options.get("extended_output");
        }

        public File getConfigFile() {
            return options.get("config");
        }

        public long getRebuildPeriod() {
            return options.get("rebuild_period");
        }

        public int getListSize() {
            return options.get("list_size");
        }
    }
}
