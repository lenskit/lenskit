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
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.data.source.PackedDataSourceBuilder;
import org.grouplens.lenskit.data.source.TextDataSourceBuilder;
import org.grouplens.lenskit.data.text.DelimitedColumnEventFormat;
import org.grouplens.lenskit.data.text.EventFormat;

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
    DataSource getSource() {
        TextDataSourceBuilder dsb = new TextDataSourceBuilder();

        String type = options.get("event_type");
        File nameFile = options.get("item_names");
        File ratingFile = options.get("csv_file");
        if (ratingFile != null) {
            return dsb.setFile(ratingFile)
                      .setDelimiter(",")
                      .build();
        }

        ratingFile = options.get("tsv_file");
        if (ratingFile != null) {
            return dsb.setFile(ratingFile)
                      .setDelimiter("\t")
                      .build();
        }

        ratingFile = options.get("ratings_file");
        if (ratingFile == null) {
            ratingFile = options.get("events_file");
        }
        if (ratingFile != null) {
            String delim = options.getString("delimiter");
            EventFormat fmt = DelimitedColumnEventFormat.create(type)
                                                        .setDelimiter(delim);
            return dsb.setFormat(fmt)
                      .setFile(ratingFile)
                      .build();
        }

        File packFile = options.get("pack_file");
        if (packFile != null) {
            return new PackedDataSourceBuilder(packFile).build();
        }

        return null;
    }

    @Nullable
    public EventDAO getEventDAO() throws IOException {
        DataSource src = getSource();
        return (src == null) ? null : src.getEventDAO();
    }

    @Nonnull
    public LenskitConfiguration getConfiguration() {
        DataSource src = getSource();
        LenskitConfiguration config = new LenskitConfiguration();
        if (src != null) {
            src.configure(config);
        }
        return config;
    }

    @Override
    public String toString() {
        DataSource src = getSource();
        return (src == null) ? "null" : src.toString();
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
