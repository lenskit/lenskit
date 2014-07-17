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

import com.google.common.base.Stopwatch;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.*;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Generate Top-N recommendations for users.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@CommandSpec(name="recommend", help="generate recommendations for users")
public class Recommend implements Command {
    private final Logger logger = LoggerFactory.getLogger(Recommend.class);
    private final Namespace options;
    private final InputData input;
    private final ScriptEnvironment environment;

    public Recommend(Namespace opts) {
        options = opts;
        input = new InputData(opts);
        environment = new ScriptEnvironment(opts);
    }

    @Override
    public void execute() throws IOException, RecommenderBuildException {
        LenskitRecommenderEngine engine = loadEngine();

        List<Long> users = options.get("users");
        final int n = options.getInt("num_recs");

        LenskitRecommender rec = engine.createRecommender();
        ItemRecommender irec = rec.getItemRecommender();
        if (irec == null) {
            logger.error("recommender has no item recommender");
            throw new UnsupportedOperationException("no item recommender");
        }

        logger.info("recommending for {} users", users.size());
        Symbol pchan = getPrintChannel();
        Stopwatch timer = Stopwatch.createStarted();
        for (long user: users) {
            List<ScoredId> recs = irec.recommend(user, n);
            System.out.format("recommendations for user %d:\n", user);
            for (ScoredId item: recs) {
                System.out.format("  %d: %.3f", item.getId(), item.getScore());
                if (pchan != null && item.hasUnboxedChannel(pchan)) {
                    System.out.format(" (%f)", item.getUnboxedChannelValue(pchan));
                }
                System.out.println();
            }
        }
        timer.stop();
        logger.info("recommended for {} users in {}", users.size(), timer);
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
        parser.addArgument("-n", "--num-recs")
              .type(Integer.class)
              .setDefault(10)
              .metavar("N")
              .help("generate up to N recommendations per user");
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
        parser.addArgument("users")
              .type(Long.class)
              .nargs("+")
              .metavar("USER")
              .help("recommend for USERS");
    }
}
