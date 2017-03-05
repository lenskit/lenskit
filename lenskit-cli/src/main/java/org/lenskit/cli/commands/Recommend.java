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
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
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

import java.io.Closeable;
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
    public void execute(Namespace opts) throws LenskitCommandException {
        Context ctx = new Context(opts);
        LenskitRecommenderEngine engine;
        try {
            engine = ctx.loader.loadEngine();
        } catch (IOException e) {
            throw new LenskitCommandException("could not load engine", e);
        }

        List<Long> users = ctx.options.get("users");
        final int n = ctx.options.getInt("num_recs");

        try (LenskitRecommender rec = engine.createRecommender(ctx.input.getDAO())) {
            ItemRecommender irec = rec.getItemRecommender();
            DataAccessObject dao = rec.getDataAccessObject();

            if (irec == null) {
                logger.error("recommender has no item recommender");
                throw new UnsupportedOperationException("no item recommender");
            }

            logger.info("recommending for {} users", users.size());
            Stopwatch timer = Stopwatch.createStarted();
            try (RecOutput output = openOutput(ctx, dao)) {
                for (long user : users) {
                    ResultList recs = irec.recommendWithDetails(user, n, null, null);
                    output.writeUser(user, recs);
                }
            }
            timer.stop();
            logger.info("recommended for {} users in {}", users.size(), timer);
        } catch (IOException e) {
            throw new LenskitCommandException("I/O error writing output", e);
        }
    }

    private RecOutput openOutput(Context ctx, DataAccessObject dao) throws IOException {
        if (ctx.options.getBoolean("json")) {
            return new JSONOutput(dao);
        } else {
            return new HumanOutput(dao);
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

        Context(Namespace opts) {
            options = opts;
            environment = new ScriptEnvironment(opts);
            input = new InputData(environment, opts);
            loader = new RecommenderLoader(input, environment, opts);
        }
    }

    private interface RecOutput extends Closeable {
        void writeUser(long user, ResultList recs) throws IOException;
    }

    private class HumanOutput implements RecOutput {
        private final DataAccessObject dao;

        HumanOutput(DataAccessObject dao) {
            this.dao = dao;
        }

        @Override
        public void writeUser(long user, ResultList recs) {
            System.out.format("recommendations for user %d:%n", user);
            for (Result res : recs) {
                System.out.format("  %d", res.getId());
                Entity item = dao.lookupEntity(CommonTypes.ITEM, res.getId());
                String name = item == null ? null : item.maybeGet(CommonAttributes.NAME);
                if (name != null) {
                    System.out.format(" (%s)", name);
                }
                System.out.format(": %.3f", res.getScore());
                System.out.println();
            }
        }

        @Override
        public void close() {

        }
    }

    private class JSONOutput implements RecOutput {
        private final DataAccessObject dao;
        private JsonGenerator generator;

        JSONOutput(DataAccessObject dao) throws IOException {
            this.dao = dao;
            JsonFactory jfac = new JsonFactory();
            generator = jfac.createGenerator(System.out)
                            .useDefaultPrettyPrinter();
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
                Entity item = dao.lookupEntity(CommonTypes.ITEM, r.getId());
                String name = item == null ? null : item.maybeGet(CommonAttributes.NAME);
                if (name != null) {
                    generator.writeStringField("name", name);
                }
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.writeEndObject();
        }

        @Override
        public void close() throws IOException {
            try {
                generator.writeEndArray();
            } finally {
                generator.close();
            }
        }
    }
}
