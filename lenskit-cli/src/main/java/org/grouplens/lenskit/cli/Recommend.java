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
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.*;
import org.grouplens.lenskit.data.dao.ItemNameDAO;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.symbols.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    private final RecommenderLoader loader;

    public Recommend(Namespace opts) {
        options = opts;
        input = new InputData(opts);
        environment = new ScriptEnvironment(opts);
        loader = new RecommenderLoader(input, environment, opts);
    }

    @Override
    public void execute() throws IOException, RecommenderBuildException {
        LenskitRecommenderEngine engine = loader.loadEngine();

        List<Long> users = options.get("users");
        final int n = options.getInt("num_recs");

        LenskitRecommender rec = engine.createRecommender();
        ItemRecommender irec = rec.getItemRecommender();
        ItemNameDAO indao = rec.get(ItemNameDAO.class);
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
                System.out.format("  %d", item.getId());
                if (indao != null) {
                    System.out.format(" (%s)", indao.getItemName(item.getId()));
                }
                System.out.format(": %.3f", item.getScore());
                if (pchan != null && item.hasUnboxedChannel(pchan)) {
                    System.out.format(" (%f)", item.getUnboxedChannelValue(pchan));
                }
                System.out.println();
            }
        }
        timer.stop();
        logger.info("recommended for {} users in {}", users.size(), timer);
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
        parser.description("Generates recommendations for a user.");
        InputData.configureArguments(parser);
        ScriptEnvironment.configureArguments(parser);
        RecommenderLoader.configureArguments(parser);
        parser.addArgument("-n", "--num-recs")
              .type(Integer.class)
              .setDefault(10)
              .metavar("N")
              .help("generate up to N recommendations per user");
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
