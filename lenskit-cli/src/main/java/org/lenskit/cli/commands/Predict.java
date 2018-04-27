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
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.RatingPredictor;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.InputData;
import org.lenskit.cli.util.RecommenderLoader;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Predict item ratings for a user.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class Predict implements Command {
    private final Logger logger = LoggerFactory.getLogger(Predict.class);

    @Override
    public String getName() {
        return "predict";
    }

    @Override
    public String getHelp() {
        return "generate predictions for a user";
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void execute(Namespace opts) throws LenskitCommandException {
        Context ctx = new Context(opts);
        LenskitRecommenderEngine engine;
        try {
            engine = ctx.loader.loadEngine();
        } catch (IOException e) {
            throw new LenskitCommandException("error loading engine", e);
        }

        long user = ctx.options.getLong("user");
        List<Long> items = ctx.options.get("items");

        try (LenskitRecommender rec = engine.createRecommender(ctx.input.getDAO())) {
            RatingPredictor pred = rec.getRatingPredictor();
            DataAccessObject dao = rec.getDataAccessObject();
            if (pred == null) {
                logger.error("recommender has no rating predictor");
                throw new UnsupportedOperationException("no rating predictor");
            }

            logger.info("predicting {} items", items.size());
            Stopwatch timer = Stopwatch.createStarted();
            Map<Long, Double> preds = pred.predict(user, items);
            System.out.format("predictions for user %d:%n", user);
            for (Map.Entry<Long, Double> e : preds.entrySet()) {
                System.out.format("  %d", e.getKey());
                Entity item = dao.lookupEntity(CommonTypes.ITEM, e.getKey());
                String name = item == null ? null : item.maybeGet(CommonAttributes.NAME);
                if (name != null) {
                    System.out.format(" (%s)", name);
                }
                System.out.format(": %.3f", e.getValue());
                System.out.println();
            }
            timer.stop();
            logger.info("predicted for {} items in {}", items.size(), timer);
        }
    }

    public void configureArguments(ArgumentParser parser) {
        parser.description("Predicts a user's rating of some items.");
        InputData.configureArguments(parser);
        ScriptEnvironment.configureArguments(parser);
        RecommenderLoader.configureArguments(parser);
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

    private static class Context {
        private final Namespace options;
        private final InputData input;
        private final ScriptEnvironment environment;
        private final RecommenderLoader loader;

        Context(Namespace opts) {
            options = opts;
            environment = new ScriptEnvironment(opts);
            input = new InputData(environment, opts);
            loader = new RecommenderLoader(input, environment, opts);
        }
    }
}
