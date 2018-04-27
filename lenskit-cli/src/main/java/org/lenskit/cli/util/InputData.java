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
package org.lenskit.cli.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.LenskitConfiguration;
import org.lenskit.data.dao.DataAccessException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.DelimitedColumnEntityFormat;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.dao.file.TextEntitySource;
import org.lenskit.data.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(InputData.class);
    private final Namespace options;
    private final ScriptEnvironment environment;

    public InputData(ScriptEnvironment env, Namespace opts) {
        environment = env;
        options = opts;
    }

    @Nullable
    public StaticDataSource getSource() {
        File sourceFile = options.get("data_source");
        if (sourceFile != null) {
            ClassLoader cl = null;
            if (environment != null) {
                cl = environment.getClassLoader();
            }
            return loadDataSource(sourceFile, cl);
        }

        StaticDataSource source = new StaticDataSource();
        TextEntitySource entities = new TextEntitySource();
        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setEntityType(CommonTypes.RATING);
        format.addColumns(CommonAttributes.USER_ID, CommonAttributes.ITEM_ID,
                          CommonAttributes.RATING, CommonAttributes.TIMESTAMP);

        String type = options.get("event_type");
        if (type != null) {
            EntityType etype = EntityType.forName(type);
            format.setEntityType(etype);
            EntityDefaults defaults = EntityDefaults.lookup(etype);
            if (defaults == null) {
                logger.warn("no defaults found for entity type {}", type);
            } else {
                format.clearColumns();
                for (TypedName<?> col: defaults.getDefaultColumns()) {
                    format.addColumn(col);
                }
            }
        }
        Integer header = options.get("header_lines");
        if (header != null) {
            format.setHeaderLines(header);
        }

        File ratingFile = options.get("csv_file");
        if (ratingFile != null) {
            format.setDelimiter(",");
            entities.setFile(ratingFile.toPath());
        }

        ratingFile = options.get("tsv_file");
        if (ratingFile != null) {
            format.setDelimiter("\t");
            entities.setFile(ratingFile.toPath());
        }

        ratingFile = options.get("ratings_file");
        if (ratingFile == null) {
            ratingFile = options.get("events_file");
        }
        if (ratingFile != null) {
            String delim = options.getString("delimiter");
            format.setDelimiter(delim);
            entities.setFile(ratingFile.toPath());
        }
        if (entities.getURL() == null) {
            // we found no configuration
            return null;
        }
        entities.setFormat(format);
        source.addSource(entities);

        File nameFile = options.get("item_names");
        if (nameFile != null) {
            TextEntitySource itemSource = new TextEntitySource();
            DelimitedColumnEntityFormat itemFormat = new DelimitedColumnEntityFormat();
            itemFormat.setDelimiter(",");
            itemFormat.setEntityType(CommonTypes.ITEM);
            itemFormat.addColumns(CommonAttributes.ENTITY_ID,
                                  CommonAttributes.NAME);
            itemSource.setFormat(itemFormat);
            itemSource.setFile(nameFile.toPath());
            source.addSource(itemSource);
        }

        return source;
    }

    private StaticDataSource loadDataSource(File sourceFile, ClassLoader loader) {
        JsonNode node;
        JsonFactory factory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        try {
            node = mapper.readTree(sourceFile);

            StaticDataSource provider = StaticDataSource.fromJSON(node, sourceFile.toURI());
            return provider;
        } catch (IOException e) {
            logger.error("error loading " + sourceFile, e);
            throw new DataAccessException("error loading " + sourceFile, e);
        }
    }

    /**
     * Get the data access object from the input data.
     * @return The data access object.
     */
    @Nullable
    public DataAccessObject getDAO() {
        StaticDataSource source = getSource();
        return source != null ? source.get() : null;
    }

    @Nonnull
    public LenskitConfiguration getConfiguration() {
        StaticDataSource src = getSource();
        LenskitConfiguration config = new LenskitConfiguration();
        if (src != null) {
            config.bind(DataAccessObject.class).toProvider(src);
        }
        return config;
    }

    @Override
    public String toString() {
        StaticDataSource src = getSource();
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
        group.addArgument("--entities-file")
             .type(File.class)
             .metavar("FILE")
             .help("read from delimited text FILE");
        options.addArgument("-d", "--delimiter")
               .setDefault(",")
               .metavar("DELIM")
               .help("input file is delimited by DELIM");
        options.addArgument("-H", "--header-lines")
               .type(Integer.class)
               .setDefault(0)
               .metavar("N")
               .help("skip N header lines at top of input file");
        options.addArgument("-t", "--input-entity-type", "--event-type")
               .setDefault("rating")
               .metavar("TYPE")
               .help("read entitites of type TYPE from input file");
        options.addArgument("--item-names")
               .type(File.class)
               .metavar("FILE")
               .help("Read item names from CSV file FILE");
        group.addArgument("--data-source")
             .type(File.class)
             .metavar("FILE")
             .help("read a data source specification from FILE");
    }
}
