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
package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.InputData;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.output.OutputFormat;
import org.lenskit.eval.crossfold.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Pack ratings data into a rating file.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class Crossfold implements Command {
    private static final Logger logger = LoggerFactory.getLogger(Crossfold.class);

    @Override
    public String getName() {
        return "crossfold";
    }

    @Override
    public String getHelp() {
        return "crossfold a data set";
    }

    public static String getDelimiter(Namespace opts) {
        return opts.get("delimiter");
    }

    @Override
    public void execute(Namespace options) throws LenskitCommandException {
        Crossfolder cf;
        try {
            cf = configureCrossfolder(options);
            cf.execute();
        } catch (IOException e) {
            throw new LenskitCommandException(e);
        }
    }

    Crossfolder configureCrossfolder(Namespace options) throws IOException {
        InputData input = new InputData(null, options);
        logger.info("packing ratings from {}", input);
        logger.debug("using delimiter {}", getDelimiter(options));

        Crossfolder cf = new Crossfolder();

        StaticDataSource src = input.getSource();
        if (src != null) {
            cf.setSource(src);
        }
        Integer k = options.get("partitions");
        if (k != null) {
            cf.setPartitionCount(k);
        }
        String name = options.get("name");
        if (name != null) {
            cf.setName(name);
        }

        OutputFormat outFmt = options.get("output_format");
        if (outFmt != null) {
            cf.setOutputFormat(outFmt);
        }
        if (!options.getBoolean("use_timestamps")) {
            cf.setWriteTimestamps(false);
        }
        cf.setEntityType(EntityType.forName(options.getString("entity_type")));

        String method = options.get("crossfold_mode");
        if (method == null) {
            method = "partition-users";
        }

        if (method.equals("partition-ratings") || method.equals("partition-entities")) {
            if (method.equals("partition-ratings")) {
                logger.warn("--partition-ratings is deprecated, use --partition-entities");
            }
            cf.setMethod(CrossfoldMethods.partitionEntities());
        } else if (method.equals("sample-entities")) {
            Integer n = options.get("sample_size");
            cf.setMethod(CrossfoldMethods.sampleEntities(n));
        } else {
            String order = options.get("order");
            SortOrder ord = order != null ? SortOrder.fromString(order) : SortOrder.RANDOM;

            HistoryPartitionMethod part = HistoryPartitions.holdout(10);
            Integer n;
            Double v;
            if ((n = options.get("holdout_count")) != null) {
                part = HistoryPartitions.holdout(n);
            }
            if ((n = options.get("retain_count")) != null) {
                part = HistoryPartitions.retain(n);
            }
            if ((v = options.get("holdout_fraction")) != null) {
                part = HistoryPartitions.holdoutFraction(v);
            }

            n = options.get("sample_size");

            switch (method) {
                case "partition-users":
                    cf.setMethod(CrossfoldMethods.partitionUsers(ord, part));
                    break;
                case "sample-users":
                    cf.setMethod(CrossfoldMethods.sampleUsers(ord, part, n));
                    break;
                case "partition-items":
                    cf.setMethod(CrossfoldMethods.partitionItems(part));
                    break;
                case "sample-items":
                    cf.setMethod(CrossfoldMethods.sampleItems(part, n));
                    break;
                default:
                    throw new IllegalArgumentException("unknown crossfold method " + method);
            }
        }

        String dir = options.get("output_dir");
        if (dir != null) {
            cf.setOutputDir(dir);
        }

        return cf;
    }

    @Override
    public void configureArguments(ArgumentParser parser) {
        parser.addArgument("-o", "--output-dir")
              .dest("output_dir")
              .type(String.class)
              .metavar("DIR")
              .help("write splits to DIR");
        parser.addArgument("-n", "--name")
              .dest("name")
              .metavar("NAME")
              .help("name the data set NAME");
        parser.addArgument("--gzip-output")
              .type(OutputFormat.class)
              .action(Arguments.storeConst())
              .setConst(OutputFormat.CSV_GZIP)
              .dest("output_format")
              .help("specify output file type");
        parser.addArgument("--no-timestamps")
              .action(Arguments.storeFalse())
              .setDefault(true)
              .dest("use_timestamps")
              .help("don't include timestamps in output");
        parser.addArgument("--entity-type")
              .metavar("TYPE")
              .setDefault("rating")
              .help("specify the type of entity to crossfold");

        parser.addArgument("-k", "--partition-count")
              .metavar("K")
              .dest("partitions")
              .type(Integer.class)
              .help("Fold into K partitions.");

        MutuallyExclusiveGroup mode =
                parser.addMutuallyExclusiveGroup("crossfold mode")
                      .description("Partitioning mode for the crossfolder.");
        mode.addArgument("--partition-users")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("partition-users")
            .help("Partition users into K partitions (the default)");
        mode.addArgument("--partition-entities")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("partition-entities")
            .help("Partition entities into K partitions");
        mode.addArgument("--partition-ratings")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("partition-ratings")
            .help("Partition ratings into K partitions");
        mode.addArgument("--sample-entities")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("sample-entities")
            .help("Create K samples of entities");
        mode.addArgument("--sample-users")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("sample-users")
            .help("Generate K samples of users");
        mode.addArgument("--partition-items")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("partition-items")
            .help("Partition items into K partitions");
        mode.addArgument("--sample-items")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("sample-items")
            .help("Generate K samples of items");

        parser.addArgument("--sample-size")
              .dest("sample_size")
              .metavar("N")
              .type(Integer.class)
              .setDefault(1000)
              .help("Sample N users per partition (for --sample-users)");

        MutuallyExclusiveGroup userOpts =
                parser.addMutuallyExclusiveGroup("user crossfolding options")
                      .description("Options controlling user-based crossfolding (--sample-users, --partition-users)");
        userOpts.addArgument("--holdout-fraction")
                .metavar("F")
                .type(Double.class)
                .dest("holdout_fraction")
                .help("Hold out a fraction of each user's ratings (for user-based folding)");
        userOpts.addArgument("--holdout-count")
                .metavar("N")
                .type(Integer.class)
                .dest("holdout_count")
                .help("Hold out N ratings per user for testing.");
        userOpts.addArgument("--retain-count")
                .metavar("N")
                .type(Integer.class)
                .dest("retain_count")
                .help("Retain N training ratings per user.");

        parser.addArgument("--timestamp-order")
              .dest("order")
              .setConst("timestamp")
              .setDefault("random")
              .action(Arguments.storeConst())
              .help("Test on latest ratings from each user, not random.");

        InputData.configureArguments(parser);
    }
}
