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
import com.google.common.base.Stopwatch;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.util.io.CompressionMode;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.LenskitRecommenderEngineBuilder;
import org.lenskit.ModelDisposition;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.InputData;
import org.lenskit.cli.util.ScriptEnvironment;
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
    public void execute(Namespace opts) throws LenskitCommandException {
        Context ctx = new Context(opts);
        LenskitConfiguration dataConfig = ctx.input.getConfiguration();
        LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
        try {
            for (LenskitConfiguration config: ctx.environment.loadConfigurations(ctx.getConfigFiles())) {
                builder.addConfiguration(config);
            }
        } catch (IOException e) {
            throw new LenskitCommandException("error loading LensKit configuration", e);
        }
        builder.addConfiguration(dataConfig, ModelDisposition.EXCLUDED);

        Stopwatch timer = Stopwatch.createStarted();
        LenskitRecommenderEngine engine = builder.build(ctx.input.getDAO());
        timer.stop();
        logger.info("built model in {}", timer);
        File output = ctx.getOutputFile();
        CompressionMode comp = CompressionMode.autodetect(output);

        logger.info("writing model to {}", output);
        try (OutputStream raw = new FileOutputStream(output);
             OutputStream stream = comp.wrapOutput(raw)) {
            engine.write(stream);
        } catch (IOException e) {
            throw new LenskitCommandException("could not write output file", e);
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

        Context(Namespace opts) {
            options = opts;
            environment = new ScriptEnvironment(opts);
            input = new InputData(environment, opts);
        }

        List<File> getConfigFiles() {
            return options.get("config");
        }

        File getOutputFile() {
            return options.get("output_file");
        }
    }
}
