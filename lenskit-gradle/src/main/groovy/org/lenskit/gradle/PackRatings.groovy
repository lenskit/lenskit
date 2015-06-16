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
import groovy.json.JsonOutput
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.lenskit.gradle.traits.DataSources
import org.lenskit.gradle.traits.SpecBuilder

/**
 * Pack a data set.
 */
class PackRatings extends LenskitTask implements DataSources, SpecBuilder {
    /**
     * The output file.  Defaults to "build/$name.pack", where $name is the name of the task.
     */
    def output

    /**
     * Control whether timestamps are included.
     */
    def boolean includeTimestamps = true

    public PackRatings() {
        conventionMapping.output = {
            "$project.buildDir/${name}.pack"
        }
    }

    /**
     * Configure the input data.  This should be an input specification in Groovy {@link JsonBuilder} syntax.  Common
     * input types should be configured with {@link #input(Map)}.
     *
     * @param input The closure expressing the input specification.
     */
    void input(Closure input) {
        spec.putAll(_rebase(input))
    }

    /**
     * Configure the input data.  For convenience, you can use the helper methods inherited from {@link DataSources} to
     * create this bit of specification.  For example:
     *
     * ```
     * input textFile {
     *     file "ratings.csv"
     *     delimiter ","
     * }
     * ```
     *
     * @param input
     */
    void input(Map input) {
        spec.putAll(_rebase(input))
    }

    /**
     * Configure an input CSV file of ratings.  Convenience method; {@link #input(Closure)} is more general.
     * @param csv A CSV file containing ratings.
     */
    void inputFile(File csv) {
        input {
            type "csv"
            file csv.toURI()
        }
    }

    @InputFile
    File guessInputFile() {
        def f = spec.get('_wrapped')?.get('file')
        if (f != null) {
            project.file(f)
        } else {
            null
        }
    }

    @OutputFile
    File getOutputFile() {
        return project.file(getOutput())
    }

    @Override
    String getCommand() {
        return 'pack-ratings'
    }

    @Override
    void doPrepare() {
        project.mkdir outputFile.parentFile
        logger.info 'preparing spec file {}', specFile
        specFile.text = JsonOutput.prettyPrint(specJSON)
    }

    @Override
    List getCommandArgs() {
        def args = []
        if (!includeTimestamps) {
            args << '--no-timestamps'
        }
        args << '--data-source' << specFile
        args << '-o'
        args << project.file(outputFile)
        args
    }

    File getSpecFile() {
        File specFile = new File(project.buildDir, "${name}.input.json")
        return specFile
    }
}
