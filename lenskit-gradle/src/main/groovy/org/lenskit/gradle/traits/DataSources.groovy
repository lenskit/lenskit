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
package org.lenskit.gradle.traits

import org.gradle.api.Project
import org.lenskit.gradle.delegates.TextDataSourceConfig

/**
 * Support for specifying various types of data sources.
 */
trait DataSources {
    abstract Project getProject()

    /**
     * Configure a text file data source.
     * @param block The configuration block, used to configure a data source.
     * @return A JSON specification of a text file data source.
     */
    def textFile(@DelegatesTo(TextDataSourceConfig) Closure block) {
        project.logger.warn('textFile is deprecated')
        def spec = new TextDataSourceConfig(project)
        spec.configure(block)
        return spec.json
    }

    /**
     * Configure a rating CSV file data source.
     * @param fn The file to use.
     * @return A JSON specification of a text file data source.
     */
    def textFile(Object fn) {
        project.logger.warn('textFile is deprecated')
        return [type: 'textfile', file: project.uri(fn).toString(),
                format: 'csv', event_type: 'rating']
    }
}
