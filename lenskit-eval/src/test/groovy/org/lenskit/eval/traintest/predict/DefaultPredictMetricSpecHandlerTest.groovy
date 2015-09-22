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
package org.lenskit.eval.traintest.predict

import groovy.json.JsonBuilder
import org.junit.Test
import org.lenskit.eval.traintest.metrics.Discounts
import org.lenskit.specs.DynamicSpec
import org.lenskit.specs.SpecUtils

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

class DefaultPredictMetricSpecHandlerTest extends GroovyTestCase {
    @Test
    void testConfigureNDCG() {
        def jsb = new JsonBuilder()
        jsb {
            type "ndcg"
            columnName "foo"
            discount "exp(5)"
        }
        def json = jsb.toString()
        def spec = SpecUtils.createMapper().reader(DynamicSpec).readValue(json)
        def metric = SpecUtils.buildObject(PredictMetric, spec)
        assertThat metric, instanceOf(NDCGPredictMetric)
        assertThat metric.columnName, equalTo("foo")
        assertThat metric.discount, equalTo(Discounts.exp(5))
    }
    @Test
    void testConfigureNDCGDefaults() {
        def jsb = new JsonBuilder()
        jsb {
            type "ndcg"
        }
        def json = jsb.toString()
        def spec = SpecUtils.createMapper().reader(DynamicSpec).readValue(json)
        def metric = SpecUtils.buildObject(PredictMetric, spec)
        assertThat metric, instanceOf(NDCGPredictMetric)
        assertThat metric.columnName, equalTo("Predict.nDCG")
        assertThat metric.discount, equalTo(Discounts.log2())
    }
}
