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
package org.lenskit.gradle.delegates

import com.fasterxml.jackson.databind.node.ObjectNode
import org.gradle.util.ConfigureUtil
import org.junit.Before
import org.junit.Test
import org.lenskit.specs.eval.PredictEvalTaskSpec

import java.nio.file.Paths

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class EvalTaskDelegateTest {
    PredictEvalTaskSpec spec
    EvalTaskDelegate delegate

    @Before
    void setupSpec() {
        spec = new PredictEvalTaskSpec()
        delegate = new EvalTaskDelegate(spec)
    }

    @Test
    void testBasicProp() {
        def block = {
            outputFile 'predictions.csv'
        }
        ConfigureUtil.configure(block, delegate)
        assertThat spec.outputFile, equalTo(Paths.get('predictions.csv'))
        assertThat spec.outputFiles, contains(Paths.get('predictions.csv'))
    }

    @Test
    void testAddMetric() {
        def block = {
            metric 'rmse'
        }
        ConfigureUtil.configure(block, delegate)
        assertThat spec.metrics, hasSize(1)
        assertThat spec.metrics*.getJSON()*.asText(), contains("rmse")
    }

    @Test
    void testAddMetricBlock() {
        def block = {
            metric('ndcg') {
                columnName 'foobar'
            }
        }
        ConfigureUtil.configure(block, delegate)
        assertThat spec.metrics, hasSize(1)
        assertThat spec.metrics*.getJSON()*.isObject(), contains(true)
        def obj = spec.metrics[0].getJSON() as ObjectNode
        assertThat obj.get('type').asText(), equalTo('ndcg')
        assertThat obj.get('columnName').asText(), equalTo('foobar')
    }
}
