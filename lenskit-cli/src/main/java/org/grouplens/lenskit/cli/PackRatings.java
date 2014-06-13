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

import com.google.common.io.Closer;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryFormatFlag;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingPacker;
import org.grouplens.lenskit.data.event.Rating;
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
@CommandSpec(name = "pack-ratings", help = "pack ratings data into a binary file")
public class PackRatings implements Command {
    private final Logger logger = LoggerFactory.getLogger(PackRatings.class);
    private final Namespace options;
    private final InputData input;

    public PackRatings(Namespace opts) {
        options = opts;
        input = new InputData(opts);
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

    @Override
    public void execute() throws IOException {
        logger.info("packing ratings from {}", input);
        logger.debug("using delimiter {}", getDelimiter());
        EventDAO dao = input.getEventDAO();
        EnumSet<BinaryFormatFlag> flags = EnumSet.noneOf(BinaryFormatFlag.class);
        if (useTimestamps()) {
            flags.add(BinaryFormatFlag.TIMESTAMPS);
        }
        logger.info("packing to {} with flags {}", getOutputFile(), flags);
        Closer closer = Closer.create();
        try {
            BinaryRatingPacker packer = closer.register(BinaryRatingPacker.open(getOutputFile(), flags));
            Cursor<Rating> ratings = closer.register(dao.streamEvents(Rating.class));
            packer.writeRatings(ratings);
            logger.info("packed {} ratings", packer.getRatingCount());
        } catch (Throwable th) {
            throw closer.rethrow(th);
        } finally {
            closer.close();
        }
    }

    public static void configureArguments(Subparser parser) {
        parser.addArgument("-o", "--output-file")
              .type(File.class)
              .metavar("FILE")
              .setDefault(new File("ratings.pack"))
              .help("pack to FILE");
        parser.addArgument("--no-timestamps")
              .action(Arguments.storeFalse())
              .dest("use_timestamps")
              .help("don't include or use timestamps");
        InputData.configureArguments(parser);
    }
}
