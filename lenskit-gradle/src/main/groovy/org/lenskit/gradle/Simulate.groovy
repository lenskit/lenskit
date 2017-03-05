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
package org.lenskit.gradle

import groovy.json.JsonOutput
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile

class Simulate extends LenskitTask {
    def File specFile
    private Object source
    private Object srcFile
    def output, extendedOutput
    def algorithm
    int listSize = 10
    long rebuildPeriod = 60 * 60 * 24

    Simulate() {
        conventionMapping.output = {
            project.file("$project.buildDir/${name}-output.csv")
        }
    }

    /**
     * Set the input source manifest.
     * @param file The path to an input source manifest file (in YAML format).
     */
    void input(Object file) {
        srcFile = file
    }

    void input(Map spec) {
        source = spec
    }

    /**
     * Configure an input CSV file of ratings.  Convenience method; {@link #input(Object)} is more general.
     * @param csv A CSV file containing ratings.
     */
    void inputFile(Object csv) {
        source = [type: "textfile",
                  file: project.uri(csv).toString(),
                  format: "csv"]
    }

    @InputFiles
    Set<File> getInputFiles() {
        def files = new HashSet()
        if (srcFile) {
            files << srcFile
        }
        // TODO Extract source files
        return files
    }

    /**
     * Set the output file.
     * @param obj The output file.
     */
    public void output(obj) {
        output = obj
    }

    /**
     * Get the primary output file for the simulation.
     * @return The CSV output file for the simulation.
     */
    @OutputFile
    public File getOutputFile() {
        return getOutput() ? project.file(getOutput()) : null
    }

    /**
     * Set the extended output file.
     * @param obj The extended output file.
     */
    public void extendedOutput(obj) {
        extendedOutput = obj
    }

    /**
     * Get the output file for the extended JSON output.
     * @return The file where the extended JSON output will be stored.
     */
    @OutputFile
    public File getExtendedOutputFile() {
        return extendedOutput ? project.file(extendedOutput) : null
    }

    public void algorithm(fn) {
        algorithm = fn
    }

    @InputFile
    public File getAlgorithmFile() {
        return algorithm ? project.file(algorithm) : null
    }

    @Override
    String getCommand() {
        return "simulate"
    }

    @Override
    List getCommandArgs() {
        def args = ['--list-size', listSize, '--rebuild-period', rebuildPeriod,
                    '--output', outputFile]
        if (srcFile != null) {
            args << "--data-source" << project.file(srcFile)
        } else {
            project.mkdir project.buildDir
            project.file("$project.buildDir/$name-input.json").text = JsonOutput.toJson(source)
            // FIXME Don't use JSON spec
            args << "--data-source" << project.file("$project.buildDir/$name-input.json")
        }
        if (extendedOutput) {
            args << '--extended-output' << extendedOutputFile
        }
        args << algorithmFile
        return args
    }
}
