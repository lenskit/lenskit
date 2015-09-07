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
package org.lenskit.eval.traintest

import org.grouplens.lenskit.data.source.CSVDataSourceBuilder
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.GlobalMeanRatingItemScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.eval.crossfold.CrossfoldMethods
import org.lenskit.eval.crossfold.Crossfolder
import org.lenskit.eval.crossfold.HistoryPartitions
import org.lenskit.eval.crossfold.SortOrder

import java.nio.file.Paths

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

/**
 * Tests for the train-test experiment.
 */
class TrainTestExperimentTest {
    TrainTestExperiment experiment

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()
    File file = null

    @Before
    void prepareFile() {
        file = folder.newFile("ratings.csv");
        file.append('1,3,3,881250949\n')
        file.append('1,5,3.5,881250949\n')
        file.append('2,5,3,881250949\n')
        file.append('2,4,3,881250949\n')
        file.append('3,1,3,881250949\n')
        file.append('3,4,3,881250949\n')
        file.append('3,5,3,881250949\n')
        file.append('5,2,3,881250949\n')
        file.append('5,1,3,881250949\n')
        file.append('5,5,3,881250949\n')
        file = folder.newFile("global-test.csv")
        file.append('1,4,3.0\n')
        file.append('3,3,4.5\n')
    }

    @Ignore("metrics changed")
    @Test
    void testAddMetric() {
        eval {
            metric new CoveragePredictMetric()
            metric new RMSEPredictMetric()
        }
        def metrics = command.getMetricFactories()
        assertThat(metrics.size(), equalTo(2))
    }

    @Ignore("metrics changed")
    @Test
    void testAddMetricsByClass() {
        eval {
            metric CoveragePredictMetric
            metric RMSEPredictMetric
        }
        def metrics = command.getMetricFactories()
        assertThat(metrics.size(), equalTo(2))
    }

    @Test
    void testSetOutput() {
        experiment.outputFile = Paths.get("eval-out.csv")
        assertThat experiment.outputFile, equalTo(Paths.get("eval-out.csv"))
    }

    @Test
    void testRun() {
        List<DataSet> sets = crossfoldRatings()
        experiment.addAlgorithm("Baseline") {
            bind ItemScorer to ItemMeanRatingItemScorer
        }
        experiment.addDataSets(sets)
        def result = experiment.run()
        assertThat(result, notNullValue())
    }

    private List<DataSet> crossfoldRatings() {
        def cf = new Crossfolder()
        cf.source = new CSVDataSourceBuilder().setFile(file).build()
        cf.partitionCount = 2
        cf.method = CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdout(1))
        cf.outputDir = folder.getRoot().toPath().resolve("splits")
        cf.execute()
        def sets = cf.dataSets
        sets
    }

    /**
     * This test attempts to reproduce <a href="https://github.com/lenskit/lenskit/issues/640">#640</a>.
     */
    @Test
    void testRunWithoutNeedingDAOs() {
        List<DataSet> sets = crossfoldRatings()
        experiment.addAlgorithm("Baseline") {
            bind ItemScorer to GlobalMeanRatingItemScorer
            bind ItemRecommender to null
        }
        experiment.addDataSets(sets)
        def result = experiment.run()
        assertThat(result, notNullValue())
    }
}
