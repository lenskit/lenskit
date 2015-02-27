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

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.EvalProject;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldMethod;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldTask;
import org.grouplens.lenskit.eval.data.crossfold.TimestampOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Pack ratings data into a rating file.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@CommandSpec(name = "crossfold", help = "crossfold a data set")
public class Crossfold implements Command {
    private final Logger logger = LoggerFactory.getLogger(Crossfold.class);
    private final Namespace options;
    private final InputData input;

    public Crossfold(Namespace opts) {
        options = opts;
        input = new InputData(null, opts);
    }

    public String getDelimiter() {
        return options.get("delimiter");
    }

    @Override
    public void execute() throws IOException, TaskExecutionException {
        logger.info("packing ratings from {}", input);
        logger.debug("using delimiter {}", getDelimiter());
        CrossfoldTask task = new CrossfoldTask();
        task.setProject(new EvalProject(System.getProperties()));
        task.setSource(input.getSource());
        task.setForce(true);

        Integer k = options.get("partitions");
        if (k != null) {
            task.setPartitions(k);
        }

        String dir = options.getString("output_dir");
        boolean pack = options.getBoolean("pack_output");
        String suffix = pack ? "pack" : "csv";
        task.setTrain(dir + "/" + "train.%d." + suffix);
        task.setTest(dir + "/" + "test.%d." + suffix);
        task.setSpec(dir + "/" + "split.%d.json");
        if (pack) {
            task.setWriteTimestamps(options.getBoolean("use_timestamps"));
        }

        CrossfoldMethod method = options.get("crossfold_mode");
        if (method == null) {
            method = CrossfoldMethod.PARTITION_USERS;
        }
        task.setMethod(method);
        Integer n;
        Double v;
        switch (method) {
        case PARTITION_RATINGS:
        case PARTITION_USERS: {
            if ((n = options.get("holdout_count")) != null) {
                task.setHoldout(n);
            } else if ((n = options.get("retain_count")) != null) {
                task.setRetain(n);
            } else if ((v = options.get("holdout_fraction")) != null) {
                task.setHoldoutFraction(v);
            }
            if ("timestamp".equals(options.getString("order"))) {
                task.setOrder(new TimestampOrder<Rating>());
            }
            break;
        }
        case SAMPLE_USERS:
            n = options.get("sample_size");
            if (n != null) {
                task.setSampleSize(n);
            }
            break;
        }

        task.execute();
    }

    public static void configureArguments(Subparser parser) {
        parser.addArgument("-o", "--output-dir")
              .dest("output_dir")
              .type(String.class)
              .metavar("DIR")
              .setDefault("crossfold")
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
            .setConst(CrossfoldMethod.PARTITION_USERS)
            .help("Partition users into K partitions (the default)");
        mode.addArgument("--partition-ratings")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst(CrossfoldMethod.PARTITION_RATINGS)
            .help("Partition ratings into K partitions");
        mode.addArgument("--sample-users")
            .dest("crossfold_mode")
            .action(Arguments.storeConst())
            .setConst(CrossfoldMethod.SAMPLE_USERS)
            .help("Generate K samples of users");

        parser.addArgument("--sample-size")
              .dest("sample_size")
              .metavar("N")
              .type(Integer.class)
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

        InputData.configureArguments(parser);
    }
}
