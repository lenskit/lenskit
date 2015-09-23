package org.lenskit.gradle.delegates

import com.fasterxml.jackson.databind.JsonNode
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.lenskit.specs.SpecUtils
import org.lenskit.specs.eval.PredictEvalTaskSpec

/**
 * Delegate for configuring evaluation tasks.
 */
class EvalTaskDelegate extends SpecDelegate {
    private final PredictEvalTaskSpec taskSpec;

    public EvalTaskDelegate(PredictEvalTaskSpec sp) {
        super(sp)
        taskSpec = sp
    }

    void metric(String name) {
        taskSpec.addMetric(name);
    }

    void metric(String name, Closure block) {
        def jsb = new JsonBuilder()
        jsb.call(block)
        def content = [type: name] + jsb.content
        def node = SpecUtils.parse(JsonNode, JsonOutput.toJson(content))
        taskSpec.addMetric(node)
    }

    Object methodMissing(String name, Object args) {
        return super.methodMissing(name, args)
    }
}
