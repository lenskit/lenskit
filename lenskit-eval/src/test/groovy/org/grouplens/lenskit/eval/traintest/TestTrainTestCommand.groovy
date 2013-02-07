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

import com.google.common.io.Files

import org.grouplens.lenskit.eval.data.CSVDataSource

import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import org.grouplens.lenskit.eval.config.CommandDelegate
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataCommand
import org.grouplens.lenskit.eval.data.traintest.TTDataSet
import org.grouplens.lenskit.eval.config.EvalScriptEngine

import static org.hamcrest.Matchers.hasKey
import org.grouplens.lenskit.symbols.Symbol
import org.apache.commons.lang3.tuple.Pair

import static org.hamcrest.Matchers.hasItem

/**
 * Tests for train-test configurations; they also serve to test the command delegate
 * framework.
 * @author Michael Ekstrand
 */
class TestTrainTestCommand {
    EvalScriptEngine engine
    TrainTestEvalCommand command
    CommandDelegate delegate

    def file = File.createTempFile("tempRatings", "csv")
    def trainTestDir = Files.createTempDir()

    @Before
    void prepareFile() {
        file.deleteOnExit()
        file.append('19,242,3,881250949\n')
        file.append('296,242,3.5,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
    }

    @Before
    void setupDelegate() {
        engine = new EvalScriptEngine()
        command = new TrainTestEvalCommand("TTcommand")
        delegate = new CommandDelegate(engine, command)
    }

    @After
    void cleanUpFiles() {
        file.delete()
        trainTestDir.deleteDir()
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
                assertThat(delegate, instanceOf(CommandDelegate))
                assertThat(resolveStrategy, equalTo(Closure.DELEGATE_FIRST))
                assertThat(delegate.command, instanceOf(GenericTTDataCommand))

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
        assertThat(ds.trainData, instanceOf(CSVDataSource))
        assertThat(ds.trainData.sourceFile, equalTo(new File("train.csv")))
        assertThat(ds.trainData.delimiter, equalTo(","))
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
        assertThat(ds.trainData, instanceOf(CSVDataSource))
        assertThat(ds.trainData.sourceFile, equalTo(new File("train.csv")))
        assertThat(ds.testData, instanceOf(CSVDataSource))
        assertThat(ds.testData.sourceFile, equalTo(new File("test.csv")))
    }

    @Test
    void testCrossfoldDataSource() {
        def dat = eval {
            crossfold("tempRatings") {
                source file
                partitions 7
                train trainTestDir.getAbsolutePath() + "/ratings.train.%d.csv"
                test trainTestDir.getAbsolutePath() + "/ratings.test.%d.csv"
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
}
