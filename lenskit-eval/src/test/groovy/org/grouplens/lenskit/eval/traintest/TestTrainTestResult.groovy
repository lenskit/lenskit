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

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.BaselineScorer
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.baseline.UserMeanItemScorer
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.eval.script.ConfigTestBase
import org.grouplens.lenskit.util.table.TableImpl
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.nullValue
import static org.junit.Assert.assertThat

/**
 * Test the result returned by the trainTest
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 *
 */
class TestTrainTestResult extends ConfigTestBase {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder()

    File sourceFile = null

    @Before
    void prepareFile() {
        sourceFile = workDir.newFile("ratings.csv")
        sourceFile.deleteOnExit()
        sourceFile.append('19,242,3,881250949\n')
        sourceFile.append('296,242,3.5,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
    }

    @Test
    void testResult() {
        def dat = eval {
            crossfold("tempRatings") {
                source sourceFile
                partitions 5
                train workDir.root.absolutePath + "/ratings.train.%d.csv"
                test workDir.root.absolutePath + "/ratings.test.%d.csv"
            }
        }
        def result = eval {
            trainTest {
                dataset dat
                output null
                assertThat(output, nullValue())
                metric MAEPredictMetric
                metric RMSEPredictMetric

                algorithm("ItemUserMean") {
                    bind ItemScorer to UserMeanItemScorer
                    bind (BaselineScorer, ItemScorer) to ItemMeanRatingItemScorer
                }
            }
        }
        assertThat(result, instanceOf(TableImpl))
    }
}
