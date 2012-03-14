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
package org.grouplens.lenskit.eval

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.junit.Test
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.baseline.BaselineRatingPredictor
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.GlobalMeanPredictor

/**
 * @author Michael Ekstrand
 */
class TestAlgorithmInstanceConfig extends ConfigTestBase {
    @Test
    void testBasicAlgorithm() {
        def obj = eval {
            algorithm("GlobalMean") {
                setComponent(RatingPredictor, BaselineRatingPredictor)
                setComponent(BaselinePredictor, GlobalMeanPredictor)
                attributes["wombat"] = "global"
            }
        }
        assertThat(obj, instanceOf(AlgorithmInstance))
        def algo = obj as AlgorithmInstance
        assertThat(algo.name, equalTo("GlobalMean"))
        assertThat(algo.attributes["wombat"] as String, equalTo("global"))
    }
}
