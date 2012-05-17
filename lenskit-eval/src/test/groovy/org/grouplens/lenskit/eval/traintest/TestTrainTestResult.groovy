/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.junit.Before

import org.junit.Test
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.baseline.BaselineRatingPredictor
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor

import static org.junit.Assert.assertThat

import static org.hamcrest.Matchers.instanceOf
import org.grouplens.lenskit.eval.util.table.ResultTable

/**
 * Test the result returned by the trainTest
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 *
 */
class TestTrainTestResult extends ConfigTestBase{
    def file = new File("ml-100k.csv")

    @Before
    void prepareFile() {
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

    @Test
    void TestResult() {
        def dat = eval{
            crossfold("ml-100k") {
                source file
                partitions 5
            }
        }
        def result = eval{
            trainTest {
                dataset dat

                metric MAEPredictMetric
                metric RMSEPredictMetric

                algorithm("ItemUserMean") {
                    bind RatingPredictor to BaselineRatingPredictor
                    bind BaselinePredictor to ItemUserMeanPredictor
                }
            }
        }
        assertThat(result, instanceOf(ResultTable))

    }
}
