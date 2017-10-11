/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.gradle.delegates

import groovy.json.JsonBuilder
import org.gradle.api.Project
import org.gradle.util.Configurable
import org.gradle.util.ConfigureUtil
import org.lenskit.gradle.traits.GradleUtils

/**
 * Delegate for configuring evaluation tasks.
 */
class EvalTaskConfig implements Configurable<EvalTaskConfig>, GradleUtils {
    /**
     * The task type.
     */
    final def String type

    final def Project project

    /**
     * The output file for individual outputs from this task.
     */
    def outputFile;

    /**
     * The list of metric configurations.
     */
    def List metrics = []

    public EvalTaskConfig(Project prj, String type) {
        project = prj
        this.type = type
    }

    /**
     * Set the output file.
     * @param file The output file.
     */
    void outputFile(file) {
        outputFile = file
    }

    /**
     * Add a metric by name.
     * @param name The metric name.
     */
    void metric(String name) {
        metrics.add name
    }

    /**
     * Add a metric with additional configuration.
     * @param name The metric name.
     * @param block The block to provide additional JSON-style configuration for the metric.
     */
    void metric(String name, Closure block) {
        def jsb = new JsonBuilder()
        jsb.call(block)
        def content = [type: name] + jsb.content
        metrics.add(content)
    }

    Map getJson() {
        return [type: type,
                output_file: makeUrl(outputFile),
                metrics: metrics]
    }

    @Override
    EvalTaskConfig configure(Closure cl) {
        try {
            return ConfigureUtil.configureSelf(cl, this)
        } catch (MissingMethodException ex) {
            if (ex.method == 'configureSelf') {
                return ConfigureUtil.configure(cl, this, false)
            } else {
                throw ex
            }
        }
    }
}
