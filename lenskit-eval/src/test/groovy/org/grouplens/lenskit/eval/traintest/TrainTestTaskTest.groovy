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
package org.grouplens.lenskit.eval.traintest

import org.apache.commons.lang3.tuple.Pair
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.baseline.UserMeanItemScorer
import org.grouplens.lenskit.data.dao.DataAccessException
import org.grouplens.lenskit.eval.EvalConfig
import org.grouplens.lenskit.eval.TaskExecutionException
import org.grouplens.lenskit.eval.data.CSVDataSource
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet
import org.grouplens.lenskit.eval.data.traintest.TTDataSet
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.eval.script.ConfigMethodInvoker
import org.grouplens.lenskit.eval.script.DefaultConfigDelegate
import org.grouplens.lenskit.eval.script.EvalScript
import org.grouplens.lenskit.eval.script.EvalScriptEngine
import org.grouplens.lenskit.symbols.Symbol
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

/**
 * Tests for train-test configurations; they also serve to test the command delegate
 * framework.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TrainTestTaskTest {
    EvalScriptEngine engine
    TrainTestEvalTask command
    DefaultConfigDelegate delegate
    EvalScript script

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()
    def file = null

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
    }

    @Before
    void setupDelegate() {
        engine = new EvalScriptEngine()
        script = new EvalScript()
        script.setEngine(engine)
        script.setProject(engine.createProject())
        command = new TrainTestEvalTask("TTcommand")
        command.setProject(script.project)
        script.project.setUserProperty(EvalConfig.THREAD_COUNT_PROPERTY, "2")
        delegate = new DefaultConfigDelegate(new ConfigMethodInvoker(engine, script.project), command)
    }

    def methodMissing(String name, args) {
        // delegate missing methods to the script for global lookup
        script.invokeMethod(name, args)
    }

    def eval(Closure cl) {
        cl.setDelegate(delegate)
        cl.setResolveStrategy(Closure.DELEGATE_FIRST)
        cl.call()
    }

    @Test
    void testAddMetric() {
        eval {
            metric new CoveragePredictMetric()
            metric new RMSEPredictMetric()
        }
        def metrics = command.getMetrics()
        assertThat(metrics.size(), equalTo(2))
    }

    @Test
    void testAddMetricsByClass() {
        eval {
            metric CoveragePredictMetric
            metric RMSEPredictMetric
        }
        def metrics = command.getMetrics()
        assertThat(metrics.size(), equalTo(2))
    }

    @Test
    void testSetOutput() {
        eval {
            output "eval-out.csv"
        }
        assertThat(command.getOutput(), equalTo(new File("eval-out.csv")))
    }

    @Test
    void testPredictOutput() {
        eval {
            predictOutput "predictions.csv"
        }
        assertThat(command.getPredictOutput(), equalTo(new File("predictions.csv")))
    }

    @Test
    void testPredictOutputAndInternalGetter() {
        eval {
            predictOutput "predictions.csv"
            assertThat(predictOutput, equalTo(new File("predictions.csv")))
        }
        assertThat(command.getPredictOutput(), equalTo(new File("predictions.csv")))
    }

    @Test
    void testGenericInput() {
        boolean closureInvoked = false
        eval {
            dataset {
                // check some things about our strategy...
                assertThat(delegate, instanceOf(DefaultConfigDelegate))
                assertThat(resolveStrategy, equalTo(Closure.DELEGATE_FIRST))
                assertThat(delegate.target, instanceOf(GenericTTDataBuilder))

                closureInvoked = true

                train csvfile("train.csv") {
                    delimiter ","
                }
                test csvfile("test.csv")
            }
        }
        assertTrue(closureInvoked)
        def data = command.dataSources()
        assertThat(data.size(), equalTo(1))
        assertThat(data.get(0), instanceOf(GenericTTDataSet))
        GenericTTDataSet ds = data.get(0) as GenericTTDataSet
        assertThat(ds.trainingData, instanceOf(CSVDataSource))
        assertThat(ds.trainingData.sourceFile, equalTo(new File("train.csv")))
        assertThat(ds.trainingData.delimiter, equalTo(","))
        assertThat(ds.testData, instanceOf(CSVDataSource))
        assertThat(ds.testData.sourceFile, equalTo(new File("test.csv")))
    }

    @Test
    void testGenericDefaults() {
        boolean closureInvoked = false
        eval {
            dataset {
                closureInvoked = true
                train "train.csv"
                test "test.csv"
            }
        }
        assertTrue(closureInvoked)
        def data = command.dataSources()
        assertThat(data.size(), equalTo(1))
        assertThat(data.get(0), instanceOf(GenericTTDataSet))
        GenericTTDataSet ds = data.get(0) as GenericTTDataSet
        assertThat(ds.trainingData, instanceOf(CSVDataSource))
        assertThat(ds.trainingData.sourceFile, equalTo(new File("train.csv")))
        assertThat(ds.testData, instanceOf(CSVDataSource))
        assertThat(ds.testData.sourceFile, equalTo(new File("test.csv")))
    }

    @Test
    void testCrossfoldDataSource() {
        def dat = eval {
            crossfold("tempRatings") {
                source file
                partitions 7
                train "${folder.root.absolutePath}/ratings.train.%d.csv"
                test "${folder.root.absolutePath}/ratings.test.%d.csv"
            }
        }
        assertThat(dat.size(), equalTo(7))
        assertThat(dat[2], instanceOf(TTDataSet))
        eval {
            dataset dat
        }
        def data = command.dataSources()
        assertThat(data.size(), equalTo(7))
    }

    @Test
    void testChannelConfig() {
        assertThat(command.predictionChannels.size(),
                   equalTo(0));
        eval {
            writePredictionChannel Symbol.of("foo")
            writePredictionChannel Symbol.of("wombat"), "woozle"
        }
        assertThat(command.predictionChannels.size(),
                   equalTo(2));
        assertThat(command.predictionChannels,
                   hasItem(Pair.of(Symbol.of("foo"), "foo")));
        assertThat(command.predictionChannels,
                   hasItem(Pair.of(Symbol.of("wombat"), "woozle")));
    }

    @Test
    void testRun() {
        eval {
            dataset crossfold("tempRatings") {
                source file
                partitions 2
                holdout 1
                train "${folder.root.absolutePath}/ratings.train.%d.csv"
                test "${folder.root.absolutePath}/ratings.test.%d.csv"
            }
            algorithm {
                bind ItemScorer to ItemMeanRatingItemScorer
            }
            output null
        }
        assertThat(command.makeJobGroups(), hasSize(1));
        assertThat(command.makeJobGroups()[0], hasSize(2));
        command.execute()
        assertThat(command.isDone(), equalTo(true))
        assertThat(command.get(), notNullValue())
    }

    @Test
    void testFailedRun() {
        eval {
            dataset crossfold("tempRatings") {
                source file
                partitions 2
                holdout 1
                train "${folder.root.absolutePath}/ratings.train.%d.csv"
                test "${folder.root.absolutePath}/ratings.test.%d.csv"
            }
            dataset("badRatings") {
                train "${folder.root.absolutePath}/noRatings.train.csv"
                test "${folder.root.absolutePath}/noRatings.test.csv"
            }
            algorithm {
                bind ItemScorer to ItemMeanRatingItemScorer
            }
            algorithm {
                bind ItemScorer to UserMeanItemScorer
            }
            output null
        }
        assertThat(command.project.config.threadCount, equalTo(2))
        assertThat(command.dataSources(), hasSize(3))
        assertThat(command.makeJobGroups(), hasSize(1));
        assertThat(command.makeJobGroups()[0], hasSize(6));
        try {
            command.execute()
            fail("command with bad ratings should fail");
        } catch (TaskExecutionException e) {
            assertThat(e.cause, anyOf(instanceOf(DataAccessException),
                                      instanceOf(IOException)))
        }
    }
}
