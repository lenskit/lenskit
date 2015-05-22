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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Crossfolder task = new Crossfolder();
        if (cfg.hasPath("name")) {
            task.setName(cfg.getString("name"));
        }
        task.setSource(context.build(DataSource.class, cfg.getConfig("source")));
        if (cfg.hasPath("partitions")) {
            task.setPartitions(cfg.getInt("partitions"));
        }

        if (cfg.hasPath("holdout")) {
            // TODO Make CrossfoldTask take holdout objects
            task.setHoldout(cfg.getInt("holdout"));
            if (cfg.hasPath("holdoutFraction")) {
                logger.warn("holdout and holdoutFraction specified, using holdout");
            }
            if (cfg.hasPath("retain")) {
                logger.warn("holdout and retain specified, using holdout");
            }
        } else if (cfg.hasPath("holdoutFraction")) {
            task.setHoldoutFraction(cfg.getDouble("holdoutFraction"));
            if (cfg.hasPath("retain")) {
                logger.warn("holdoutFraction and retain specified, using holdout");
            }
        } else if (cfg.hasPath("retain")) {
            task.setRetain(cfg.getInt("retain"));
        }

        if (cfg.hasPath("order")) {
            String order = cfg.getString("order");
            if (order.equalsIgnoreCase("random")) {
                task.setOrder(new RandomOrder<Rating>());
            } else if (order.equalsIgnoreCase("timestamp")) {
                task.setOrder(new TimestampOrder<Rating>());
            } else {
                throw new SpecificationException("invalid order " + order + " for crossfold");
            }
        }

        if (cfg.hasPath("useTimestamps")) {
            task.setWriteTimestamps(cfg.getBoolean("useTimestamps"));
        }

        if (cfg.hasPath("outputDir")) {
            // TODO Make CrossfoldTask use an output directory
            String dir = cfg.getString("outputDir");
            boolean pack = cfg.hasPath("packOutput") && cfg.getBoolean("packOutput");
            String suffix = pack ? "pack" : "csv";
            task.setTrain(dir + "/train.%d." + suffix);
            task.setTest(dir + "/test.%d." + suffix);
            task.setSpec(dir + "/split.%d.json");
        } else {
            logger.warn("no output directory specified for crossfold {}", task.getName());
        }

        if (cfg.hasPath("isolate")) {
            task.setIsolate(cfg.getBoolean("isolate"));
        }

        return task;
    }
}
