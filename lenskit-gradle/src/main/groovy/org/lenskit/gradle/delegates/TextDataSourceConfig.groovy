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

import org.gradle.api.Project
import org.gradle.util.Configurable
import org.gradle.util.ConfigureUtil

/**
 * Delegate for configuring a data source.
 */
class TextDataSourceConfig implements Configurable<TextDataSourceConfig> {
    final def Project project

    def file
    def String delimiter = "\t"
    def int headerLines = 0

    public TextDataSourceConfig(Project prj) {
        project = prj
    }

    void file(Object f) {
        file = f
    }

    void delimiter(String delim) {
        delimiter = delim
    }

    void headerLines(int n) {
        headerLines = n
    }

    Map<String,Object> getJson() {
        return [type: 'textfile',
                file: project.uri(file).toString(),
                format: 'delimited',
                delimiter: delimiter,
                header: headerLines,
                event_type: 'rating']
    }

    @Override
    TextDataSourceConfig configure(Closure cl) {
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
