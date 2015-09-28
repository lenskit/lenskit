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

import com.fasterxml.jackson.databind.JsonNode
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.lenskit.specs.SpecUtils
import org.lenskit.specs.eval.EvalTaskSpec
import org.lenskit.specs.eval.PredictEvalTaskSpec

/**
 * Delegate for configuring evaluation tasks.
 */
class EvalTaskDelegate extends SpecDelegate {
    private final EvalTaskSpec taskSpec;

    public EvalTaskDelegate(EvalTaskSpec sp) {
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
