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
