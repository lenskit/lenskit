/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval.data.traintest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.grouplens.lenskit.dtree.DataNode;
import org.grouplens.lenskit.dtree.Trees;
import org.grouplens.lenskit.eval.EvaluatorConfigurationException;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.DataSourceProvider;
import org.grouplens.lenskit.util.spi.ConfigAlias;
import org.grouplens.lenskit.util.spi.ServiceFinder;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure a crossfolding train-test provider. It takes an underlying data
 * source and sets it up for <i>k</i>-fold cross-validation. The following child
 * elements are supported:
 * 
 * <dl>
 * <dt>holdout
 * <dd>The number of items to hold out from each user.
 * <dt>folds
 * <dd>The number of sets to partition users into.
 * <dt>mode
 * <dd>The holdout mode. <tt>random</tt> (the default) or <tt>timestamp</tt>.
 * <dt>sources
 * <dd>A list of input sources to build crossfold sets from.
 * <dt>database
 * <dd>If <tt>true</tt>, store cross-folded data in databases in the evaluator
 * cache.  Otherwise, splits are cached in memory.
 * </dl>
 * 
 * <p>
 * The partitioned data sets are saved in database files, one for each fold.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
@ConfigAlias("crossfold")
@MetaInfServices
public class CrossfoldTTDataProvider implements TTDataProvider {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldTTDataProvider.class);

    @Override
    public List<TTDataSet> configure(DataNode config)
            throws EvaluatorConfigurationException {
        logger.debug("Configuring crossfolder from {}", config);
        int holdout = Trees.childValueInt(config, "holdout", 10);
        int folds = Trees.childValueInt(config, "folds", 5);
        HoldoutMode mode =
            HoldoutMode.fromString(Trees.childValue(config, "mode", "random"));

        List<DataSource> sources = configureSources(config);
        if (sources.isEmpty()) {
            throw new EvaluatorConfigurationException(
                    "No crossfold inputs configured");
        }

        List<TTDataSet> ttSources = new ArrayList<TTDataSet>();
        for (DataSource src: sources) {
            CrossfoldManager cx =
                new CrossfoldManager(src, holdout, folds, mode);
            for (int i = 1; i <= folds; i++) {
                ttSources.add(new CrossfoldTTDataSet(cx, i));
            }
        }
        return ttSources;
    }

    /**
     * Configure the input sources for this crossfolder.
     * 
     * @param config The configuration node for some input sources.
     * @return The input sources configured by the <tt>sources</tt> node.
     * @throws EvaluatorConfigurationException if there is a configuration
     *         error.
     */
    private List<DataSource> configureSources(DataNode config)
            throws EvaluatorConfigurationException {
        logger.debug("Looking for sources in {}", config);
        DataNode snode = Trees.child(config, "sources");
        if (snode == null) {
            return Collections.emptyList();
        }

        List<DataSource> sources = new ArrayList<DataSource>();
        ServiceFinder<DataSourceProvider> finder =
            ServiceFinder.get(DataSourceProvider.class);
        for (DataNode sn: snode.getChildren()) {
            logger.debug("Getting data provider {}", sn.getName());
            DataSourceProvider dsp = finder.findProvider(sn.getName());
            if (dsp == null) {
                throw new EvaluatorConfigurationException(
                        "Unknown data source " + sn.getName());
            }
            sources.addAll(dsp.configure(sn));
        }
        return sources;
    }

}
