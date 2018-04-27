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
package org.lenskit.build

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
                path = path.replaceAll(~/.*(org\/(?:grouplens\/)?lenskit\/.*)\.class/) { m ->
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
