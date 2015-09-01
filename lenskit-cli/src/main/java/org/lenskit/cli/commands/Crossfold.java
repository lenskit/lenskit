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
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.data.ratings.Rating;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.lenskit.cli.Command;
import org.lenskit.cli.util.InputData;
import org.lenskit.eval.crossfold.*;
import org.lenskit.specs.SpecUtils;
import org.lenskit.specs.eval.CrossfoldSpec;
import org.lenskit.specs.eval.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Pack ratings data into a rating file.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class Crossfold implements Command {
    private final Logger logger = LoggerFactory.getLogger(Crossfold.class);

    @Override
    public String getName() {
        return "crossfold";
    }

    @Override
    public String getHelp() {
        return "crossfold a data set";
    }

    public String getDelimiter(Namespace opts) {
        return opts.get("delimiter");
    }

    @Override
    public void execute(Namespace options) throws IOException, TaskExecutionException {
        InputData input = new InputData(null, options);
        logger.info("packing ratings from {}", input);
        logger.debug("using delimiter {}", getDelimiter(options));
        Crossfolder cf;
        File specFile = options.get("spec");
        if (specFile != null) {
            if (!specFile.exists()) {
                logger.error("Spec file {} does not exist", specFile);
                throw new FileNotFoundException("specification " + specFile);
            }
            CrossfoldSpec spec = SpecUtils.load(CrossfoldSpec.class, specFile.toPath());
            cf = Crossfolder.fromSpec(spec);
        } else {
            cf = new Crossfolder();
        }

        DataSource src = input.getSource();
        if (src != null) {
            cf.setSource(src);
        }
        Integer k = options.get("partitions");
        if (k != null) {
            cf.setPartitionCount(k);
        }

        String dir = options.get("output_dir");
        if (dir != null) {
            cf.setOutputDir(dir);
        }
        if (options.getBoolean("pack_output")) {
            cf.setOutputFormat(OutputFormat.PACK);
        }
        if (!options.getBoolean("use_timestamps")) {
            cf.setWriteTimestamps(false);
        }

        String method = options.get("crossfold_mode");
        if (method == null) {
            method = "partition-users";
        }

        if (method.equals("partition-ratings")) {
            cf.setMethod(SplitMethods.partitionRatings());
        } else {
            String order = options.get("order");
            Order<Rating> ord = new RandomOrder<>();
            if (order != null && order.equals("timestamp")) {
                ord = new TimestampOrder<>();
            }

            PartitionAlgorithm<Rating> part = new HoldoutNPartition<>(10);
            Integer n;
            Double v;
            if ((n = options.get("holdout_count")) != null) {
                part = new HoldoutNPartition<>(n);
            }
            if ((n = options.get("retain_count")) != null) {
                part = new RetainNPartition<>(n);
            }
            if ((v = options.get("holdout_fraction")) != null) {
                part = new FractionPartition<>(v);
            }

            n = options.get("sample_size");

            if (method.equals("partition-users")) {
                cf.setMethod(SplitMethods.partitionUsers(ord, part));
            } else if (method.equals("sample-users")) {
                cf.setMethod(SplitMethods.sampleUsers(ord, part, n));
            }
        }

        cf.execute();
    }

    public void configureArguments(ArgumentParser parser) {
        parser.addArgument("-o", "--output-dir")
              .dest("output_dir")
              .type(String.class)
              .metavar("DIR")
              .help("write splits to DIR");
        parser.addArgument("--pack-output")
              .action(Arguments.storeTrue())
              .dest("pack_output")
              .help("store output in binary-packed files");
        parser.addArgument("--no-timestamps")
              .action(Arguments.storeFalse())
              .setDefault(true)
              .dest("use_timestamps")
              .help("don't include timestamps in output");

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
        mode.addArgument("--partition-ratings")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("partition-ratings")
            .help("Partition ratings into K partitions");
        mode.addArgument("--sample-users")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst("sample-users")
            .help("Generate K samples of users");

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
                .action(Arguments.storeConst())
                .help("Test on latest ratings from each user, not random.");

        parser.addArgument("spec")
              .type(File.class)
              .metavar("SPEC")
              .nargs("?")
              .help("Read crossfold configuration from SPEC (command line opts will override)");

        InputData.configureArguments(parser);
    }
}
