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
package org.lenskit.gradle.traits

import groovy.json.JsonDelegate
import org.gradle.api.Project

/**
 * Support for specifying various types of data sources.
 */
trait DataSources {
    abstract Project getProject()

    /**
     * Configure a text file data source.
     * @param block The configuration block.
     * @return A JSON specification of a text file data source.
     * @see TextFileDelegate
     */
    Map<String,Object> textFile(@DelegatesTo(TextFileDelegate) Closure block) {
        def dlg = new TextFileDelegate(project)
        def c = block.clone() as Closure
        c.setDelegate(dlg)
        c.setResolveStrategy(Closure.DELEGATE_FIRST)
        c.call()
        dlg.spec
    }

    static class TextFileDelegate extends ConfigDelegate {
        private final Project project

        TextFileDelegate(Project prj) {
            project = prj
            spec.type = 'text'
        }

        /**
         * Configure the input file of the text source.
         * @param obj The input file (passed to {@link Project#file(Object)}).
         */
        void file(obj) {
            spec.file = project.file(obj).toURI().toString()
        }

        /**
         * Configure the text file delimiter.  The default delimiter is ",".
         * @param delim The delimiter
         */
        void delimiter(String delim) {
            spec.delimiter = delim
        }

        /**
         * Configure the data source name.
         * @param name The data source name.
         */
        void name(String name) {
            spec.name = name
        }

        /**
         * Configure the preference domain.
         * @param dom A map configuring the preference domain.  Valid keys are `minimum`, `maximum`, and `precision`.
         */
        void domain(Map dom) {
            spec.domain = dom
        }

        /**
         * Configure the preference domain with a closure.
         * @param dom A closure configuring the preference domain, using JsonBuilder syntax.  Valid keys are `minimum`,
         * `maximum`, and `precision`.
         */
        void domain(Closure dom) {
            spec.domain = JsonDelegate.cloneDelegateAndGetContent(dom)
        }
    }
}
