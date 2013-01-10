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
package org.grouplens.lenskit.eval

import org.grouplens.lenskit.iterative.ThresholdStoppingCondition

import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.BaselineRatingPredictor
import org.grouplens.lenskit.baseline.GlobalMeanPredictor
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.grouplens.lenskit.iterative.params.MinimumIterations
import org.grouplens.lenskit.iterative.params.StoppingThreshold
import org.grouplens.lenskit.transform.threshold.RealThreshold
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author Michael Ekstrand
 */
class TestAlgorithmInstanceConfig extends ConfigTestBase {
    @Test
    void testBasicAlgorithm() {
        def obj = eval {
            algorithm("GlobalMean") {
                bind RatingPredictor to BaselineRatingPredictor
                bind BaselinePredictor to GlobalMeanPredictor

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
                root ThresholdStoppingCondition
                within(RealThreshold) {
                    set StoppingThreshold to 0.1d
                }
                within(ThresholdStoppingCondition) {
                    set StoppingThreshold to 0.001d
                    set MinimumIterations to 42
                }
            }
        }
        def algo = obj as AlgorithmInstance
        def fact = algo.getFactory()
        fact.setDAOFactory(new EventCollectionDAO.Factory([]))
        def engine = fact.create()
        def rec = engine.open()
        try {
            def stop = rec.get(ThresholdStoppingCondition)
            assertThat(stop.threshold,
                       closeTo(0.001d, 1.0e-1))
            assertThat(stop.minimumIterations, equalTo(42))
            def thresh = rec.get(RealThreshold)
            assertThat(thresh.value,
                       closeTo(0.1d, 1.0e-1));
        } finally {
            rec.close()
        }
    }
}
