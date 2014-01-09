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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.config.ConfigurationLoader;
import org.grouplens.lenskit.core.*;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@CommandSpec(name="train-model", help="train a recommender model")
public class TrainModel implements Command {
    private final Logger logger = LoggerFactory.getLogger(TrainModel.class);
    private final Namespace options;
    private final ScriptEnvironment environment;
    private final InputData input;

    public TrainModel(Namespace opts) {
        options = opts;
        environment = new ScriptEnvironment(opts);
        input = new InputData(opts);
    }

    public List<File> getConfigFiles() {
        return options.get("config");
    }

    public File getOutputFile() {
        return options.get("output_file");
    }

    @Override
    public void execute() throws IOException, RecommenderBuildException {
        LenskitConfiguration dataConfig = input.getConfiguration();
        LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
        for (LenskitConfiguration config: environment.loadConfigurations(getConfigFiles())) {
            builder.addConfiguration(config);
        }
        builder.addConfiguration(dataConfig, ModelDisposition.EXCLUDED);

        Stopwatch timer = new Stopwatch();
        timer.start();
        LenskitRecommenderEngine engine = builder.build();
        timer.stop();
        logger.info("built model in {}", timer);
        File output = getOutputFile();
        logger.info("writing model to {}", output);
        Closer closer = Closer.create();
        try {
            OutputStream stream = closer.register(new FileOutputStream(output));
            if (LKFileUtils.isCompressed(output)) {
                stream = closer.register(new GZIPOutputStream(stream));
            }
            engine.write(stream);
        } catch (Throwable th) {
            throw closer.rethrow(th);
        } finally {
            closer.close();
        }
    }

    public static void configureArguments(ArgumentParser parser) {
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
}
