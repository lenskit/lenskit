package org.lenskit.gradle.delegates

import com.fasterxml.jackson.databind.JsonNode
import groovy.json.JsonBuilder
import org.lenskit.specs.SpecUtils
import org.lenskit.specs.eval.PredictEvalTaskSpec

/**
 * Delegate for configuring evaluation tasks.
 */
public class EvalTaskDelegate extends SpecDelegate {
    private final PredictEvalTaskSpec taskSpec;

    public EvalTaskDelegate(PredictEvalTaskSpec sp) {
        super(sp)
        taskSpec = sp
    }

    void metric(String name) {
        taskSpec.addMetric(name);
    }

    void metric(String name, Closure block) {
        def jsb = new JsonBuilder([type: name])
        jsb.call(block)
        def node = SpecUtils.parse(JsonNode, jsb.toString())
        taskSpec.addMetric(node)
    }

    @Override
    Object invokeMethod(String name, Object args) {
        return super.invokeMethod(name, args)
    }
}
