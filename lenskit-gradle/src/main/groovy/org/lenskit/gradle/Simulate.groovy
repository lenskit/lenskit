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
package org.lenskit.gradle

import groovy.json.JsonOutput
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile

class Simulate extends LenskitTask {
    private Object source
    private Object srcFile
    def output, extendedOutput
    def algorithm
    int listSize = 10
    long rebuildPeriod = 60 * 60 * 24

    Simulate() {
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
