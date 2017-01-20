/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.cli.Command;
import org.lenskit.cli.util.InputData;
import org.lenskit.cli.util.RecommenderLoader;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.data.dao.ItemNameDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Generate Top-N recommendations for users.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class Recommend implements Command {
    private final Logger logger = LoggerFactory.getLogger(Recommend.class);

    @Override
    public String getName() {
        return "recommend";
    }

    @Override
    public String getHelp() {
        return "generate recommendations for users";
    }

    @Override
    public void execute(Namespace opts) throws IOException, RecommenderBuildException {
        Context ctx = new Context(opts);
        LenskitRecommenderEngine engine = ctx.loader.loadEngine();

        List<Long> users = ctx.options.get("users");
        final int n = ctx.options.getInt("num_recs");

        try (LenskitRecommender rec = engine.createRecommender()) {
            ItemRecommender irec = rec.getItemRecommender();
            ItemNameDAO indao = rec.get(ItemNameDAO.class);
            RecOutput output;
            if (ctx.options.getBoolean("json")) {
                output = new JSONOutput(indao);
            } else {
                output = new HumanOutput(indao);
            }

            if (irec == null) {
                logger.error("recommender has no item recommender");
                throw new UnsupportedOperationException("no item recommender");
            }

            logger.info("recommending for {} users", users.size());
            Stopwatch timer = Stopwatch.createStarted();
            output.begin();
            for (long user : users) {
                ResultList recs = irec.recommendWithDetails(user, n, null, null);
                output.writeUser(user, recs);
            }
            output.end();
            timer.stop();
            logger.info("recommended for {} users in {}", users.size(), timer);
        }
    }

    public void configureArguments(ArgumentParser parser) {
        parser.description("Generates recommendations for a user.");
        InputData.configureArguments(parser);
        ScriptEnvironment.configureArguments(parser);
        RecommenderLoader.configureArguments(parser);
        parser.addArgument("--json")
              .action(Arguments.storeTrue())
              .help("output in JSON instead of human-readable format");
        parser.addArgument("-n", "--num-recs")
              .type(Integer.class)
              .setDefault(10)
              .metavar("N")
              .help("generate up to N recommendations per user");
        parser.addArgument("users")
              .type(Long.class)
              .nargs("+")
              .metavar("USER")
              .help("recommend for USERS");
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

    private static interface RecOutput {
        void begin() throws IOException;
        void writeUser(long user, ResultList recs) throws IOException;
        void end() throws IOException;
    }

    private class HumanOutput implements RecOutput {
        private final ItemNameDAO nameDAO;

        HumanOutput(ItemNameDAO ind) {
            nameDAO = ind;
        }

        @Override
        public void begin() {
        }

        @Override
        public void writeUser(long user, ResultList recs) {
            System.out.format("recommendations for user %d:%n", user);
            for (Result item : recs) {
                System.out.format("  %d", item.getId());
                if (nameDAO != null) {
                    System.out.format(" (%s)", nameDAO.getItemName(item.getId()));
                }
                System.out.format(": %.3f", item.getScore());
                System.out.println();
            }
        }

        @Override
        public void end() {

        }
    }

    private class JSONOutput implements RecOutput {
        private final ItemNameDAO nameDAO;
        private JsonGenerator generator;

        JSONOutput(ItemNameDAO ind) throws IOException {
            nameDAO = ind;
            JsonFactory jfac = new JsonFactory();
            generator = jfac.createGenerator(System.out)
                            .useDefaultPrettyPrinter();
        }

        @Override
        public void begin() throws IOException {
            generator.writeStartArray();
        }

        @Override
        public void writeUser(long user, ResultList recs) throws IOException {
            generator.writeStartObject();
            generator.writeNumberField("user", user);
            generator.writeArrayFieldStart("recommendations");
            for (Result r: recs) {
                generator.writeStartObject();
                generator.writeNumberField("item", r.getId());
                generator.writeNumberField("score", r.getScore());
                if (nameDAO != null) {
                    generator.writeStringField("name", nameDAO.getItemName(r.getId()));
                }
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.writeEndObject();
        }

        @Override
        public void end() throws IOException {
            generator.writeEndArray();
            generator.close();
        }
    }
}
