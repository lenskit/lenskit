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
        ConfigureUtil.configure(cl, this, false)
        return this
    }
}
