/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.*;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Predict item ratings for a user.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@CommandSpec(name="predict", help="generate predictions for a user")
public class Predict implements Command {
    private final Logger logger = LoggerFactory.getLogger(Predict.class);
    private final Namespace options;
    private final InputData input;
    private final ScriptEnvironment environment;

    public Predict(Namespace opts) {
        options = opts;
        input = new InputData(opts);
        environment = new ScriptEnvironment(opts);
    }

    @Override
    public void execute() throws IOException, RecommenderBuildException {
        LenskitRecommenderEngine engine = loadEngine();

        long user = options.getLong("user");
        List<Long> items = options.get("items");

        LenskitRecommender rec = engine.createRecommender();
        RatingPredictor pred = rec.getRatingPredictor();
        if (pred == null) {
            logger.error("recommender has no rating predictor");
            throw new UnsupportedOperationException("no rating predictor");
        }

        logger.info("predicting {} items", items.size());
        Symbol pchan = getPrintChannel();
        Stopwatch timer = Stopwatch.createStarted();
        SparseVector preds = pred.predict(user, items);
        Long2ObjectMap channel = null;
        if (pchan != null) {
            for (TypedSymbol sym: preds.getChannelSymbols()) {
                if (sym.getRawSymbol().equals(pchan)) {
                    channel = preds.getChannel(sym);
                }
            }
        }
        for (VectorEntry e: preds.fast()) {
            System.out.format("  %d: %.3f", e.getKey(), e.getValue());
            if (channel != null) {
                System.out.format(" (%s)", channel.get(e.getKey()));
            }
            System.out.println();
        }
        timer.stop();
        logger.info("predicted for {} items in {}", items.size(), timer);
    }

    private LenskitRecommenderEngine loadEngine() throws RecommenderBuildException, IOException {
        File modelFile = options.get("model_file");
        if (modelFile == null) {
            logger.info("creating fresh recommender");
            LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
            for (LenskitConfiguration config: environment.loadConfigurations(getConfigFiles())) {
                builder.addConfiguration(config);
            }
            builder.addConfiguration(input.getConfiguration());
            Stopwatch timer = Stopwatch.createStarted();
            LenskitRecommenderEngine engine = builder.build();
            timer.stop();
            logger.info("built recommender in {}", timer);
            return engine;
        } else {
            logger.info("loading recommender from {}", modelFile);
            LenskitRecommenderEngineLoader loader = LenskitRecommenderEngine.newLoader();
            for (LenskitConfiguration config: environment.loadConfigurations(getConfigFiles())) {
                loader.addConfiguration(config);
            }
            loader.addConfiguration(input.getConfiguration());
            Stopwatch timer = Stopwatch.createStarted();
            LenskitRecommenderEngine engine;
            InputStream input = new FileInputStream(modelFile);
            try {
                if (LKFileUtils.isCompressed(modelFile)) {
                    input = new GZIPInputStream(input);
                }
                engine = loader.load(input);
            } finally {
                input.close();
            }
            timer.stop();
            logger.info("loaded recommender in {}", timer);
            return engine;
        }
    }

    List<File> getConfigFiles() {
        return options.getList("config_file");
    }

    Symbol getPrintChannel() {
        String name = options.get("print_channel");
        if (name == null) {
            return null;
        } else {
            return Symbol.of(name);
        }
    }

    public static void configureArguments(ArgumentParser parser) {
        InputData.configureArguments(parser);
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("-c", "--config-file")
              .type(File.class)
              .action(Arguments.append())
              .metavar("FILE")
              .help("use configuration from FILE");
        parser.addArgument("-m", "--model-file")
              .type(File.class)
              .metavar("FILE")
              .help("load model from FILE");
        parser.addArgument("--print-channel")
              .metavar("CHAN")
              .help("also print value from CHAN");
        parser.addArgument("user")
              .type(Long.class)
              .metavar("USER")
              .help("predict for USER");
        parser.addArgument("items")
              .type(Long.class)
              .metavar("ITEM")
              .nargs("+")
              .help("predict for ITEMs");
    }
}
