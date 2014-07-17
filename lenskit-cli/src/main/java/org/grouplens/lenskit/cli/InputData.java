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

import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SimpleFileRatingDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingFile;

import java.io.File;
import java.io.IOException;

/**
 * Helper class for managing input data.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class InputData {
    private final Namespace options;

    public InputData(Namespace opts) {
        options = opts;
    }

    Source getSource() {
        File ratingFile = options.get("csv_file");
        if (ratingFile != null) {
            return new TextInput(ratingFile, ",");
        }

        ratingFile = options.get("tsv_file");
        if (ratingFile != null) {
            return new TextInput(ratingFile, "\t");
        }

        ratingFile = options.get("ratings_file");
        if (ratingFile != null) {
            String delim = options.getString("delimiter");
            return new TextInput(ratingFile, delim);
        }

        File packFile = options.get("pack_file");
        if (packFile != null) {
            return new PackedInput(packFile);
        }

        throw new IllegalStateException("no input data configured");
    }

    public EventDAO getEventDAO() throws IOException {
        return getSource().getEventDAO();
    }

    public LenskitConfiguration getConfiguration() {
        return getSource().getConfiguration();
    }

    @Override
    public String toString() {
        return getSource().toString();
    }

    static interface Source {
        EventDAO getEventDAO() throws IOException;
        LenskitConfiguration getConfiguration();
    }

    static class TextInput implements Source {
        final File inputFile;
        final String delimiter;

        public TextInput(File file, String delim) {
            inputFile = file;
            delimiter = delim;
        }

        @Override
        public EventDAO getEventDAO() {
            return SimpleFileRatingDAO.create(inputFile, delimiter);
        }

        @Override
        public LenskitConfiguration getConfiguration() {
            LenskitConfiguration config = new LenskitConfiguration();
            config.addComponent(getEventDAO());
            return config;
        }

        @Override
        public String toString() {
            return "file " + inputFile + " with delimiter '" + delimiter + "'";
        }
    }

    static class PackedInput implements Source {
        final File inputFile;

        public PackedInput(File file) {
            inputFile = file;
        }

        @Override
        public EventDAO getEventDAO() throws IOException {
            return BinaryRatingDAO.open(inputFile);
        }

        @Override
        public LenskitConfiguration getConfiguration() {
            LenskitConfiguration config = new LenskitConfiguration();
            config.addComponent(BinaryRatingDAO.class);
            config.bind(BinaryRatingFile.class, File.class)
                  .to(inputFile);
            return config;
        }

        @Override
        public String toString() {
            return "packed file " + inputFile;
        }
    }

    public static void configureArguments(ArgumentParser parser) {
        MutuallyExclusiveGroup group =
                parser.addMutuallyExclusiveGroup("input data")
                      .description("Specify the input data for the command.")
                      .required(true);
        ArgumentGroup options = parser.addArgumentGroup("input options")
                                      .description("Additional options for input data.");
        group.addArgument("--csv-file")
             .type(File.class)
             .metavar("FILE")
             .help("read from comma-separated FILE");
        group.addArgument("--tsv-file")
             .type(File.class)
             .metavar("FILE")
             .help("read from tab-separated FILE");
        group.addArgument("--ratings-file")
             .type(File.class)
             .metavar("FILE")
             .help("read from delimited text FILE");
        options.addArgument("-d", "--delimiter")
               .setDefault(",")
               .metavar("DELIM")
               .help("input file is delimited by DELIM");
        group.addArgument("--pack-file")
             .type(File.class)
             .metavar("FILE")
             .help("read from binary packed FILE");
    }
}
