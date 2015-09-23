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
