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
package org.grouplens.lenskit.eval.algorithm

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.GlobalMeanRatingItemScorer
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.eval.script.ConfigTestBase
import org.grouplens.lenskit.eval.traintest.ExternalAlgorithm
import org.grouplens.lenskit.iterative.MinimumIterations
import org.grouplens.lenskit.iterative.StoppingThreshold
import org.grouplens.lenskit.iterative.ThresholdStoppingCondition
import org.grouplens.lenskit.transform.threshold.AbsoluteThreshold
import org.grouplens.lenskit.transform.threshold.RealThreshold
import org.grouplens.lenskit.transform.threshold.ThresholdValue
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class AlgorithmInstanceConfigTest extends ConfigTestBase {
    @Test
    void testBasicAlgorithm() {
        def obj = eval {
            algorithm("GlobalMean") {
                bind ItemScorer to GlobalMeanRatingItemScorer

                attributes["wombat"] = "global"
            }
        }
        assertThat(obj, instanceOf(AlgorithmInstance))
        def algo = obj as AlgorithmInstance
        assertThat(algo.name, equalTo("GlobalMean"))
        assertThat(algo.attributes["wombat"] as String, equalTo("global"))
    }

    @Test
    void testWithinBlock() {
        def obj = eval {
            algorithm("TestFoo") {
                root RealThreshold
                root AbsoluteThreshold
                root ThresholdStoppingCondition
                within(RealThreshold) {
                    set ThresholdValue to 0.1d
                }
                within(AbsoluteThreshold) {
                    set ThresholdValue to 0.5d
                }
                within(ThresholdStoppingCondition) {
                    set StoppingThreshold to 0.001d
                    set MinimumIterations to 42
                }
                bind EventDAO to new EventCollectionDAO([])
            }
        }
        def algo = obj as AlgorithmInstance
        def rec = algo.buildRecommender(null);
        def stop = rec.get(ThresholdStoppingCondition)
        assertThat(stop.threshold,
                   closeTo(0.001d, 1.0e-6d))
        assertThat(stop.minimumIterations, equalTo(42))
        def thresh = rec.get(RealThreshold)
        assertThat(thresh.value,
                   closeTo(0.1d, 1.0e-6d))
        def athresh = rec.get(AbsoluteThreshold)
        assertThat(athresh.value,
                   closeTo(0.5d, 1.0e-6d))
    }

    @Test
    void testAtBlock() {
        def obj = eval {
            algorithm("TestFoo") {
                root RealThreshold
                root AbsoluteThreshold
                root ThresholdStoppingCondition
                at(RealThreshold) {
                    set ThresholdValue to 0.1d
                }
                at(AbsoluteThreshold) {
                    set ThresholdValue to 0.5d
                }
                at(ThresholdStoppingCondition) {
                    set StoppingThreshold to 0.001d
                    set MinimumIterations to 42
                }
                bind EventDAO to new EventCollectionDAO([])
            }
        }
        def algo = obj as AlgorithmInstance
        def rec = algo.buildRecommender(null);
        def stop = rec.get(ThresholdStoppingCondition)
        assertThat(stop.threshold,
                   closeTo(0.001d, 1.0e-5d))
        assertThat(stop.minimumIterations, equalTo(42))
        def thresh = rec.get(RealThreshold)
        assertThat(thresh.value,
                   closeTo(0.1d, 1.0e-5d))
        def athresh = rec.get(AbsoluteThreshold)
        assertThat(athresh.value,
                   closeTo(0.5d, 1.0e-5d))
    }

    @Test
    void testExternalAlgorithm() {
        def obj = eval {
            externalAlgorithm("Cheater") {
                command (["cat", "{TEST_DATA}"])

                attributes["wombat"] = "global"
            }
        }
        assertThat(obj, instanceOf(ExternalAlgorithm))
        def algo = obj as ExternalAlgorithm
        assertThat(algo.name, equalTo("Cheater"))
        assertThat(algo.attributes["wombat"] as String, equalTo("global"))
        assertThat(algo.command, equalTo(["cat", "{TEST_DATA}"].toList()))
    }
}
