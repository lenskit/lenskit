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
package org.grouplens.lenskit.cli;

import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineBuilder;
import org.grouplens.lenskit.core.ModelDisposition;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class TrainModel implements Command {
    private final Logger logger = LoggerFactory.getLogger(TrainModel.class);

    @Override
    public String getName() {
        return "train-model";
    }

    @Override
    public String getHelp() {
        return "train a recommender model";
    }

    @Override
    public void execute(Namespace opts) throws IOException, RecommenderBuildException {
        Context ctx = new Context(opts);
        LenskitConfiguration dataConfig = ctx.input.getConfiguration();
        LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
        for (LenskitConfiguration config: ctx.environment.loadConfigurations(ctx.getConfigFiles())) {
            builder.addConfiguration(config);
        }
        builder.addConfiguration(dataConfig, ModelDisposition.EXCLUDED);

        Stopwatch timer = Stopwatch.createStarted();
        LenskitRecommenderEngine engine = builder.build();
        timer.stop();
        logger.info("built model in {}", timer);
        File output = ctx.getOutputFile();
        CompressionMode comp = CompressionMode.autodetect(output);

        logger.info("writing model to {}", output);
        try (OutputStream raw = new FileOutputStream(output);
             OutputStream stream = comp.wrapOutput(raw)) {
            engine.write(stream);
        }
    }

    public void configureArguments(ArgumentParser parser) {
        parser.description("Trains a recommendation model and write it to disk.");
        ScriptEnvironment.configureArguments(parser);
        InputData.configureArguments(parser);
        parser.addArgument("-o", "--output-file")
              .type(File.class)
              .metavar("FILE")
              .setDefault("model.bin")
              .help("write trained model to FILE");
        parser.addArgument("config")
              .type(File.class)
              .nargs("+")
              .metavar("CONFIG")
              .help("load algorithm configuration from CONFIG");
    }

    private static class Context {
        private final Namespace options;
        private final ScriptEnvironment environment;
        private final InputData input;

        public Context(Namespace opts) {
            options = opts;
            environment = new ScriptEnvironment(opts);
            input = new InputData(environment, opts);
        }

        public List<File> getConfigFiles() {
            return options.get("config");
        }

        public File getOutputFile() {
            return options.get("output_file");
        }
    }
}
