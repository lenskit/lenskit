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
package org.lenskit.gradle

import groovy.json.JsonBuilder
import groovy.json.JsonDelegate
import groovy.json.JsonOutput
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFiles

/**
 * Crossfold a data set.  This task can only crossfold a single data set; multiple tasks must be used to produce
 * multiple cross-validation splits.
 *
 * In addition to the methods and properties specified in this class, the crossfolder also supports all configuration
 * directives supported by the crossfold operation.  For example, you can say:
 *
 * ```
 * includeTimestamps false
 * partitions 10
 * ```
 */
class Crossfold extends LenskitTask {
    /**
     * The crossfold specification.
     */
    private JsonDelegate specBuilder = new JsonDelegate()

    /**
     * The output directory for cross-validation.  Defaults to "build/$name.out", where $name is the name of the task.
     */
    def outputDir

    public Crossfold() {
        conventionMapping.outputDir = {
            "$project.buildDir/${name}.out"
        }
    }

    /**
     * Configure the input data.  This should be an input specification in Groovy {@link JsonBuilder} syntax.  For
     * example:
     *
     * ```
     * input {
     *     type "text"
     *     file "ratings.csv"
     *     delimiter ","
     *     domain {
     *         minimum 1.0
     *         maximum 5.0
     *         precision 1.0
     *     }
     * }
     * ```
     *
     * @param input The closure expressing the input specification.
     */
    void input(Closure input) {
        specBuilder.input {
            _base_uri project.rootDir.toURI().toString()
            _wrapped input
        }
    }

    /**
     * Configure an input CSV file of ratings.  Convenience method; {@link #input(Closure)} is more general.
     * @param csv A CSV file containing ratings.
     */
    void inputFile(File csv) {
        input {
            type "csv"
            file csv.toString()
        }
    }

    def methodMissing(String name, def args) {
        specBuilder.invokeMethod(name, args)
    }

    @InputFile
    File guessInputFile() {
        def map = specBuilder.content
        def f = map?.get('input')?.get('_wrapped')?.get('file')
        if (f != null) {
            project.file(f)
        } else {
            null
        }
    }

    @OutputFiles
    FileCollection getOutputFiles() {
        return project.fileTree(getOutputDir()) {
            exclude 'crossfold.json'
        }
    }

    @Override
    String getCommand() {
        return 'crossfold'
    }

    @Override
    void doPrepare() {
        project.mkdir getOutputDir()
        logger.info 'preparing spec file {}', specFile
        specBuilder.content['name'] = name
        specFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(specBuilder.content))
    }

    @Override
    List getCommandArgs() {
        def args = []
        args << '-o'
        args << project.file(getOutputDir())
        args << specFile
    }

    File getSpecFile() {
        File specFile = new File(project.file(getOutputDir()), "crossfold.json")
        return specFile
    }
}
