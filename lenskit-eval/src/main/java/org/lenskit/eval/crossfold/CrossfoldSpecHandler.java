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
package org.lenskit.eval.crossfold;

import com.typesafe.config.Config;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.specs.SpecHandler;
import org.grouplens.lenskit.specs.SpecificationContext;
import org.grouplens.lenskit.specs.SpecificationException;
import org.lenskit.eval.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Specification handler to configure a crossfold task from a specification.
 */
public class CrossfoldSpecHandler implements SpecHandler<Crossfolder> {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldSpecHandler.class);

    public boolean handlesType(String type) {
        return "crossfold".equals(type);
    }

    @Override
    public Crossfolder buildFromSpec(SpecificationContext context, Config cfg) throws SpecificationException {
        Crossfolder cf = new Crossfolder();
        if (cfg.hasPath("name")) {
            cf.setName(cfg.getString("name"));
        }
        cf.setSource(context.build(DataSource.class, cfg.getConfig("source")));
        if (cfg.hasPath("partitions")) {
            cf.setPartitionCount(cfg.getInt("partitions"));
        }

        String method = cfg.hasPath("method") ? cfg.getString("method") : "partition-users";
        switch (method) {
        case "partition-users": {
            PartitionAlgorithm<Rating> partition = getRatingPartitionAlgorithm(cfg);
            Order<Rating> order = getRatingOrder(cfg);
            cf.setMethod(CrossfoldMethods.partitionUsers(order, partition));
        }
        case "sample-users": {
            PartitionAlgorithm<Rating> partition = getRatingPartitionAlgorithm(cfg);
            Order<Rating> order = getRatingOrder(cfg);
            int sampleSize = cfg.hasPath("sampleSize") ? cfg.getInt("sampleSize") : 1000;
            cf.setMethod(CrossfoldMethods.sampleUsers(order, partition, sampleSize));
        }
        case "partition-ratings":
            cf.setMethod(CrossfoldMethods.partitionRatings());
            break;
        default:
            throw new SpecificationException("invalid crossfold method " + method);
        }

        if (cfg.hasPath("useTimestamps")) {
            cf.setWriteTimestamps(cfg.getBoolean("useTimestamps"));
        }

        if (cfg.hasPath("outputDir")) {
            cf.setOutputDir(cfg.getString("outputDir"));
        } else {
            logger.warn("no output directory specified for crossfold {}", cf.getName());
        }

        if (cfg.hasPath("outputFormat")) {
            switch (cfg.getString("outputFormat")) {
            case "pack":
                cf.setOutputFormat(OutputFormat.PACK);
                break;
            case "gzip":
                cf.setOutputFormat(OutputFormat.CSV_GZIP);
                break;
            case "xz":
                cf.setOutputFormat(OutputFormat.CSV_GZIP);
                break;
            default:
                throw new SpecificationException("invalid output format " + cfg.getString("outputFormat"));
            }
        }

        if (cfg.hasPath("isolate")) {
            cf.setIsolate(cfg.getBoolean("isolate"));
        }

        return cf;
    }

    @Nonnull
    private PartitionAlgorithm<Rating> getRatingPartitionAlgorithm(Config cfg) {
        PartitionAlgorithm<Rating> partition = new HoldoutNPartition<>(10);
        if (cfg.hasPath("holdout")) {
            partition = new HoldoutNPartition<>(cfg.getInt("holdout"));
            if (cfg.hasPath("holdoutFraction")) {
                logger.warn("holdout and holdoutFraction specified, using holdout");
            }
            if (cfg.hasPath("retain")) {
                logger.warn("holdout and retain specified, using holdout");
            }
        } else if (cfg.hasPath("holdoutFraction")) {
            partition = new FractionPartition<>(cfg.getDouble("holdoutFraction"));
            if (cfg.hasPath("retain")) {
                logger.warn("holdoutFraction and retain specified, using holdout");
            }
        } else if (cfg.hasPath("retain")) {
            partition = new RetainNPartition<>(cfg.getInt("retain"));
        }
        return partition;
    }

    @Nonnull
    private Order<Rating> getRatingOrder(Config cfg) throws SpecificationException {
        Order<Rating> order = new RandomOrder<>();
        if (cfg.hasPath("order")) {
            switch (cfg.getString("order").toLowerCase()) {
            case "random":
                order = new RandomOrder<>();
                break;
            case "timestamp":
                order = new TimestampOrder<>();
                break;
            default:
                throw new SpecificationException("invalid order " + cfg.getString("order") + " for crossfold");
            }
        }
        return order;
    }
}
