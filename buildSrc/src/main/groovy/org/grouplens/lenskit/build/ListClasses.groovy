package org.grouplens.lenskit.build

import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class ListClasses extends SourceTask {
    def output = null

    @OutputFile
    File getOutputFile() {
        return output == null ? null : project.file(output)
    }

    @TaskAction
    public void listClasses() {
        if (outputFile == null) {
            throw new IllegalStateException("$name: no output file configured")
        }
        project.mkdir outputFile.parentFile
        def classFiles = source.matching {
            include '**/*.class'
        }
        outputFile.withPrintWriter { out ->
            for (cf in classFiles) {
                def path = cf.absolutePath.replace('\\', '/')
                // extract org.grouplens... and sanitize
                path = path.replaceAll(~/.*(org\/grouplens.*)\.class/) { m ->
                    // convert slashes and $ to dots
                    m[1].replaceAll(~/[\/$]/, '.')
                }
                if (!(path =~ /\.\d+$/)) { // no anonymous classes
                    out.println path
                }
            }
        }
    }
}
