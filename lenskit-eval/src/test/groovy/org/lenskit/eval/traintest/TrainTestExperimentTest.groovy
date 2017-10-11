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
package org.lenskit.eval.traintest

import com.google.common.base.Charsets
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.baseline.GlobalMeanRatingItemScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.basic.ConstantItemScorer
import org.lenskit.basic.PopularityRankItemScorer
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.eval.crossfold.CrossfoldMethods
import org.lenskit.eval.crossfold.Crossfolder
import org.lenskit.eval.crossfold.HistoryPartitions
import org.lenskit.eval.crossfold.SortOrder
import org.lenskit.eval.traintest.predict.PredictEvalTask
import org.lenskit.eval.traintest.recommend.RecommendEvalTask
import org.lenskit.eval.traintest.recommend.TopNMRRMetric
import org.lenskit.knn.item.ItemItemScorer
import org.lenskit.transform.normalize.MeanCenteringVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.lenskit.transform.normalize.VectorNormalizer

import javax.inject.Provider
import java.nio.file.Files
import java.nio.file.Paths

import static org.hamcrest.Matchers.*
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

        def mtFile = folder.newFile("multi-test.csv")
        mtFile.append('1,4,3.0\n')
        mtFile.append('3,3,4.5\n')
        mtFile.append('1,2,4.5\n')

        def djfile = folder.newFile("disjoint-test.csv")
        djfile.append("6,3,4.5\n")
        djfile.append("6,4,3.2\n")

        experiment = new TrainTestExperiment()
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
        def predT = new PredictEvalTask()
        experiment.addTask(predT)
        def result = experiment.execute()
        assertThat(result, notNullValue())
        assertThat(result, hasSize(2))
        assertThat(result.column("Succeeded"), everyItem(equalTo('Y')))
    }

    /**
     * This test attempts to reproduce <a href="https://github.com/lenskit/lenskit/issues/838">#838</a>.
     */
    @Test
    void testDisjointTestSet() {
        DataSet set = DataSet.newBuilder("test")
                .setTrain(StaticDataSource.csvRatingFile(folder.root.toPath().resolve("ratings.csv")))
                .setTest(StaticDataSource.csvRatingFile(folder.root.toPath().resolve("disjoint-test.csv")))
                .build()
        experiment.addAlgorithm("Baseline") {
            bind ItemScorer to ItemItemScorer
            within (UserVectorNormalizer) {
                bind VectorNormalizer to MeanCenteringVectorNormalizer
            }
        }
        experiment.addDataSet(set)
        def predT = new PredictEvalTask()
        experiment.addTask(predT)
        def result = experiment.execute()
        assertThat(result, notNullValue())
        assertThat(result.column("RMSE.ByUser"), contains(nullValue()))
    }

    private List<DataSet> crossfoldRatings() {
        def cf = new Crossfolder()
        cf.source = StaticDataSource.csvRatingFile(file.toPath())
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
        def result = experiment.execute()
        assertThat(result, notNullValue())
    }

    @Test
    public void testMultipleAlgorithms() {
        def cfg = folder.newFile("algos.groovy")
        cfg.text = '''import org.lenskit.baseline.*
import org.lenskit.api.ItemScorer

algorithm('A1') {
    attributes['foo'] = 'bar'
    bind ItemScorer to GlobalMeanRatingItemScorer
}
algorithm('A2') {
    attributes['foo'] = 'bat'
    bind ItemScorer to UserMeanItemScorer
}
'''
        experiment.addAlgorithms(cfg.toPath())
        assertThat(experiment.algorithms, hasSize(2))
        assertThat(experiment.algorithms*.name,
                   contains('A1', 'A2'))
        assertThat(experiment.algorithms[0].attributes,
                   hasEntry('foo', 'bar'))
        assertThat(experiment.algorithms[1].attributes,
                   hasEntry('foo', 'bat'))
    }

    @Test
    void testContinueAfterError() {
        List<DataSet> sets = crossfoldRatings()
        def errp = new Provider<Double>() {
            @Override
            Double get() {
                throw new UnsupportedOperationException("ni")
            }
        }
        experiment.addAlgorithm("Fail") {
            bind ItemScorer to ConstantItemScorer
            bind (ConstantItemScorer.Value,double) toProvider errp
        }
        experiment.addAlgorithm("Baseline") {
            bind ItemScorer to ItemMeanRatingItemScorer
        }
        experiment.addDataSets(sets)
        experiment.continueAfterError = true
        experiment.parallelTasks = 1
        experiment.threadCount = 1
        def predT = new PredictEvalTask()
        experiment.addTask(predT)
        def result = experiment.execute()
        assertThat(result, notNullValue())
        assertThat(result, hasSize(4))
        assertThat(result.column("Succeeded"),
                   containsInAnyOrder('Y', 'Y', 'N', 'N'))
    }

    @Test
    void testSeparateTopN() {
        DataSet set = DataSet.newBuilder("test")
                .setTrain(StaticDataSource.csvRatingFile(folder.root.toPath().resolve("ratings.csv")))
                .setTest(StaticDataSource.csvRatingFile(folder.root.toPath().resolve("multi-test.csv")))
                .build()
        experiment.addDataSet(set)
        experiment.addAlgorithm("Baseline") {
            bind ItemScorer to PopularityRankItemScorer
        }
        def task = new RecommendEvalTask()
        def itemFile = folder.root.toPath().resolve("item-output.csv")
        task.separateItems = true
        task.itemOutputFile = itemFile
        task.addMetric(new TopNMRRMetric())
        experiment.addTask(task)
        def result = experiment.execute()
        assertThat(result.column("MRR"), notNullValue())
        assertThat(result.column("MRR"), hasSize(1))
        assertThat(Files.exists(itemFile), equalTo(true))
        def csvP = CSVParser.parse(itemFile.toFile(), Charsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader())
        def csv = csvP.iterator().toList()*.toMap()
        // 3 test entities
        assertThat(csv, hasSize(3))
        // from 2 different users
        assertThat(csv*.User.toSet(), containsInAnyOrder("1", "3"))
        assertThat(csv*.TargetItem.toSet(), containsInAnyOrder("2", "3", "4"))
        assertThat(csv*.RecipRank*.toDouble().sum() / 3.0d, closeTo(result.column("MRR").get(0), 1.0e-6d ))
    }

    @Test
    void testMissingItemRecommender() {
        List<DataSet> sets = crossfoldRatings()
        experiment.addAlgorithm("Baseline") {
            bind ItemScorer to ItemMeanRatingItemScorer
            bind ItemRecommender to null
        }
        experiment.addDataSets(sets)
        def task = new RecommendEvalTask()
        experiment.addTask(task)
        task = new PredictEvalTask()
        experiment.addTask(task)
        def result = experiment.execute()
        assertThat(result, notNullValue())
        assertThat(result, hasSize(2))
        assertThat(result.column("Succeeded"), everyItem(equalTo('Y')))
    }

    @Test
    void testMissingRatingPredictor() {
        List<DataSet> sets = crossfoldRatings()
        experiment.addAlgorithm("Baseline") {
            bind ItemScorer to PopularityRankItemScorer
            bind RatingPredictor to null
        }
        experiment.addDataSets(sets)
        def task = new RecommendEvalTask()
        experiment.addTask(task)
        task = new PredictEvalTask()
        experiment.addTask(task)
        def result = experiment.execute()
        assertThat(result, notNullValue())
        assertThat(result, hasSize(2))
        assertThat(result.column("Succeeded"), everyItem(equalTo('Y')))
    }
}
