/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
