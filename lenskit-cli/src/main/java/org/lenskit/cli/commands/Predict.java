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
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.data.dao.ItemNameDAO;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.cli.Command;
import org.lenskit.cli.util.InputData;
import org.lenskit.cli.util.RecommenderLoader;
import org.lenskit.cli.util.ScriptEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

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
    public void execute(Namespace opts) throws IOException, RecommenderBuildException {
        Context ctx = new Context(opts);
        LenskitRecommenderEngine engine = ctx.loader.loadEngine();

        long user = ctx.options.getLong("user");
        List<Long> items = ctx.options.get("items");

        LenskitRecommender rec = engine.createRecommender();
        RatingPredictor pred = rec.getRatingPredictor();
        ItemNameDAO names = rec.get(ItemNameDAO.class);
        if (pred == null) {
            logger.error("recommender has no rating predictor");
            throw new UnsupportedOperationException("no rating predictor");
        }

        logger.info("predicting {} items", items.size());
        Symbol pchan = getPrintChannel(ctx);
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
        System.out.format("predictions for user %d:%n", user);
        for (VectorEntry e: preds) {
            System.out.format("  %d", e.getKey());
            if (names != null) {
                System.out.format(" (%s)", names.getItemName(e.getKey()));
            }
            System.out.format(": %.3f", e.getValue());
            if (channel != null) {
                System.out.format(" (%s)", channel.get(e.getKey()));
            }
            System.out.println();
        }
        timer.stop();
        logger.info("predicted for {} items in {}", items.size(), timer);
    }

    Symbol getPrintChannel(Context ctx) {
        String name = ctx.options.get("print_channel");
        if (name == null) {
            return null;
        } else {
            return Symbol.of(name);
        }
    }

    public void configureArguments(ArgumentParser parser) {
        parser.description("Predicts a user's rating of some items.");
        InputData.configureArguments(parser);
        ScriptEnvironment.configureArguments(parser);
        RecommenderLoader.configureArguments(parser);
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

    private static class Context {
        private final Namespace options;
        private final InputData input;
        private final ScriptEnvironment environment;
        private final RecommenderLoader loader;

        public Context(Namespace opts) {
            options = opts;
            environment = new ScriptEnvironment(opts);
            input = new InputData(environment, opts);
            loader = new RecommenderLoader(input, environment, opts);
        }
    }
}
