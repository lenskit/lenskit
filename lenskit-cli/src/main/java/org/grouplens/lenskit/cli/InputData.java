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
import org.grouplens.lenskit.data.dao.MapItemNameDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingDAO;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingFile;
import org.grouplens.lenskit.data.text.CSVFileItemNameDAOProvider;
import org.grouplens.lenskit.data.text.DelimitedColumnEventFormat;
import org.grouplens.lenskit.data.text.ItemFile;
import org.grouplens.lenskit.data.text.TextEventDAO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nullable
    Source getSource() {
        String type = options.get("event_type");
        File nameFile = options.get("item_names");
        File ratingFile = options.get("csv_file");
        if (ratingFile != null) {
            return new TextInput(ratingFile, ",", type, nameFile);
        }

        ratingFile = options.get("tsv_file");
        if (ratingFile != null) {
            return new TextInput(ratingFile, "\t", type, nameFile);
        }

        ratingFile = options.get("ratings_file");
        if (ratingFile == null) {
            ratingFile = options.get("events_file");
        }
        if (ratingFile != null) {
            String delim = options.getString("delimiter");
            return new TextInput(ratingFile, delim, type, nameFile);
        }

        File packFile = options.get("pack_file");
        if (packFile != null) {
            return new PackedInput(packFile, nameFile);
        }

        return null;
    }

    @Nullable
    public EventDAO getEventDAO() throws IOException {
        Source src = getSource();
        return (src == null) ? null : src.getEventDAO();
    }

    @Nonnull
    public LenskitConfiguration getConfiguration() {
        Source src = getSource();
        if (src == null) {
            return new LenskitConfiguration();
        } else {
            return src.getConfiguration();
        }
    }

    @Override
    public String toString() {
        Source src = getSource();
        return (src == null) ? "null" : src.toString();
    }

    static interface Source {
        EventDAO getEventDAO() throws IOException;
        LenskitConfiguration getConfiguration();
    }

    static class TextInput implements Source {
        final File inputFile;
        final String delimiter;
        final String type;
        final File nameFile;

        public TextInput(File file, String delim, String et, File nf) {
            inputFile = file;
            delimiter = delim;
            type = et;
            nameFile = nf;
        }

        @Override
        public EventDAO getEventDAO() {
            DelimitedColumnEventFormat format = DelimitedColumnEventFormat.create(type);
            format.setDelimiter(delimiter);
            return TextEventDAO.create(inputFile, format);
        }

        @Override
        public LenskitConfiguration getConfiguration() {
            LenskitConfiguration config = new LenskitConfiguration();
            config.addComponent(getEventDAO());
            if (nameFile != null) {
                config.bind(MapItemNameDAO.class)
                      .toProvider(CSVFileItemNameDAOProvider.class);
                config.set(ItemFile.class)
                      .to(nameFile);
            }
            return config;
        }

        @Override
        public String toString() {
            return "file " + inputFile + " with delimiter '" + delimiter + "'";
        }
    }

    static class PackedInput implements Source {
        final File inputFile;
        final File nameFile;

        public PackedInput(File file, File nf) {
            inputFile = file;
            nameFile = nf;
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
            if (nameFile != null) {
                config.bind(MapItemNameDAO.class)
                      .toProvider(CSVFileItemNameDAOProvider.class);
                config.set(ItemFile.class)
                      .to(nameFile);
            }
            return config;
        }

        @Override
        public String toString() {
            return "packed file " + inputFile;
        }
    }

    public static void configureArguments(ArgumentParser parser) {
        configureArguments(parser, false);
    }

    public static void configureArguments(ArgumentParser parser, boolean required) {
        MutuallyExclusiveGroup group =
                parser.addMutuallyExclusiveGroup("input data")
                      .description("Specify the input data for the command.")
                      .required(required);
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
        group.addArgument("--events-file")
             .type(File.class)
             .metavar("FILE")
             .help("read from delimited text FILE");
        options.addArgument("-d", "--delimiter")
               .setDefault(",")
               .metavar("DELIM")
               .help("input file is delimited by DELIM");
        options.addArgument("-t", "--event-type")
               .setDefault("rating")
               .metavar("TYPE")
               .help("read events of type TYPE from input file");
        options.addArgument("--item-names")
               .type(File.class)
               .metavar("FILE")
               .help("Read item names from CSV file FILE");
        group.addArgument("--pack-file")
             .type(File.class)
             .metavar("FILE")
             .help("read from binary packed FILE");
    }
}
