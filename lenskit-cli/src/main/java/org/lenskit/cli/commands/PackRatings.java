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
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryFormatFlag;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingPacker;
import org.grouplens.lenskit.data.event.Rating;
import org.lenskit.cli.Command;
import org.lenskit.cli.util.InputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Pack ratings data into a rating file.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class PackRatings implements Command {
    private final Logger logger = LoggerFactory.getLogger(PackRatings.class);

    @Override
    public String getName() {
        return "pack-ratings";
    }

    @Override
    public String getHelp() {
        return "pack ratings data into a binary file";
    }

    @Override
    public void execute(Namespace opts) throws IOException {
        Context ctx = new Context(opts);
        logger.info("packing ratings from {}", ctx.input);
        logger.debug("using delimiter {}", ctx.getDelimiter());
        EventDAO dao = ctx.input.getEventDAO();
        if (dao == null) {
            throw new IOException("no data source specified");
        }
        EnumSet<BinaryFormatFlag> flags = EnumSet.noneOf(BinaryFormatFlag.class);
        if (ctx.useTimestamps()) {
            flags.add(BinaryFormatFlag.TIMESTAMPS);
        }
        logger.info("packing to {} with flags {}", ctx.getOutputFile(), flags);
        try (BinaryRatingPacker packer = BinaryRatingPacker.open(ctx.getOutputFile(), flags);
        Cursor<Rating> ratings = dao.streamEvents(Rating.class)) {
            packer.writeRatings(ratings);
            logger.info("packed {} ratings", packer.getRatingCount());
        }
    }

    @Override
    public void configureArguments(ArgumentParser parser) {
        parser.description("Takes a ratings data set and writes it in binary packed format to a " +
                           "data file.");
        parser.addArgument("-o", "--output-file")
              .type(File.class)
              .metavar("FILE")
              .setDefault(new File("ratings.pack"))
              .help("pack to FILE");
        parser.addArgument("--no-timestamps")
              .action(Arguments.storeFalse())
              .dest("use_timestamps")
              .help("don't include or use timestamps");
        InputData.configureArguments(parser, true);
    }

    private static class Context {
        private final Namespace options;
        private final InputData input;

        public Context(Namespace opts) {
            options = opts;
            input = new InputData(null, opts);
        }

        public File getOutputFile() {
            return options.get("output_file");
        }

        public String getDelimiter() {
            return options.get("delimiter");
        }

        public boolean useTimestamps() {
            return options.getBoolean("use_timestamps");
        }
    }
}
