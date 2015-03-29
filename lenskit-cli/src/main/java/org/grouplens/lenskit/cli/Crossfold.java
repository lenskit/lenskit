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

import com.google.auto.service.AutoService;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.eval.EvalProject;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldMethod;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldTask;
import org.grouplens.lenskit.specs.SpecificationContext;
import org.grouplens.lenskit.specs.SpecificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Pack ratings data into a rating file.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class Crossfold implements Command {
    private final Logger logger = LoggerFactory.getLogger(Crossfold.class);

    private static final String DEFAULT_SPEC =
            "outputDir: crossfold";

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
    public void execute(Namespace options) throws IOException, TaskExecutionException, SpecificationException {
        InputData input = new InputData(null, options);
        logger.info("packing ratings from {}", input);
        logger.debug("using delimiter {}", getDelimiter(options));
        // FIXME Make the command-line arguments generate an overriding spec
        Config defaults = ConfigFactory.parseString(DEFAULT_SPEC);
        Config spec = null;
        File specFile = options.get("spec");
        if (specFile != null) {
            spec = ConfigFactory.parseFile(specFile);
            spec = spec.withFallback(defaults);
        } else {
            spec = defaults;
        }

        Map<String,Object> overrides = Maps.newHashMap();

        DataSource src = input.getSource();
        if (src != null) {
            overrides.put("source", src.toSpecification(SpecificationContext.create()));
        }
        Integer k = options.get("partitions");
        if (k != null) {
            overrides.put("partitions", k);
        }

        String dir = options.get("output_dir");
        if (dir != null) {
            overrides.put("outputDir", dir);
        }
        if (options.getBoolean("pack_output")) {
            overrides.put("packOutput", true);
        }
        if (!options.getBoolean("use_timestamps")) {
            overrides.put("useTimestamps", false);
        }

        CrossfoldMethod method = options.get("crossfold_mode");
        if (method != null) {
            switch (method) {
            case PARTITION_RATINGS:
                overrides.put("mode", "partition-ratings");
                break;
            case PARTITION_USERS:
                overrides.put("mode", "partition-users");
                break;
            case SAMPLE_USERS:
                overrides.put("mode", "sample-users");
                break;
            }
        }

        Integer n;
        Double v;
        if ((n = options.get("holdout_count")) != null) {
            overrides.put("holdout", n);
        }
        if ((n = options.get("retain_count")) != null) {
            overrides.put("retain", n);
        }
        if ((v = options.get("holdout_fraction")) != null) {
            overrides.put("holdoutFraction", v);
        }
        if ((n = options.get("sample_size")) != null) {
            overrides.put("sampleSize", n);
        }
        String order = options.getString("order");
        if (order != null) {
            overrides.put("order", order);
        }

        Config finalSpec = ConfigFactory.parseMap(overrides).withFallback(spec);
        CrossfoldTask task = SpecificationContext.create().build(CrossfoldTask.class, finalSpec);

        task.setForce(true);
        task.setProject(new EvalProject(System.getProperties()));
        task.execute();
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

        parser.addArgument("spec")
              .type(File.class)
              .metavar("SPEC")
              .nargs("?")
              .help("Read crossfold configuration from SPEC (command line opts will override)");

        InputData.configureArguments(parser);
    }
}
