/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.test;

import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstanceCommand;
import org.grouplens.lenskit.eval.config.EvalConfig;
import org.grouplens.lenskit.eval.data.GenericDataSource;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldCommand;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric;
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric;
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric;
import org.grouplens.lenskit.eval.traintest.TrainTestEvalCommand;
import org.grouplens.lenskit.util.table.Table;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * A test suite that does cross-validation of an algorithm.
 */
public abstract class CrossfoldTestSuite extends ML100KTestSuite {
    protected String workDirName = System.getProperty("lenskit.temp.dir");

    protected abstract void configureAlgorithm(LenskitRecommenderEngineFactory factory);

    protected abstract void checkResults(Table table);

    @Test
    public void testAlgorithmAccuracy() throws CommandException {
        if (workDirName == null) {
            throw new IllegalStateException("must configure lenskit.temp.dir");
        }
        File work = new File(workDirName);

        EvalConfig config = new EvalConfig();
        LenskitAlgorithmInstanceCommand algo = new LenskitAlgorithmInstanceCommand();
        configureAlgorithm(algo.getFactory());

        CrossfoldCommand cross = new CrossfoldCommand("ml-100k");
        cross.setConfig(config);
        cross.setSource(new GenericDataSource("ml-100k", daoFactory));
        cross.setPartitions(5);
        cross.setHoldout(0.2);
        cross.setTrain(new File(work, "train.%d.csv").getAbsolutePath());
        cross.setTest(new File(work, "test.%d.csv").getAbsolutePath());

        TrainTestEvalCommand tt = new TrainTestEvalCommand("train-test");
        tt.setConfig(config);
        tt.addAlgorithm(algo.call());
        for (TTDataSet data: cross.call()) {
            tt.addDataset(data);
        }

        tt.addMetric(new CoveragePredictMetric());
        tt.addMetric(new RMSEPredictMetric());
        tt.addMetric(new MAEPredictMetric());

        tt.setOutput(null);

        Table result = tt.call();
        assertThat(result, notNullValue());
        checkResults(result);
    }
}
