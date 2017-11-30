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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.lenskit.gradle.traits.DataSources

/**
 * Crossfold a data set.  This task can only crossfold a single data set; multiple tasks must be used to produce
 * multiple cross-validation splits.
 *
 * @see DataSources
 * @see <http://mooc.lenskit.org/documentation/evaluator/data/>
 */
class Crossfold extends LenskitTask implements DataSources, DataSetProvider {
    /**
     * The output directory for cross-validation.  Defaults to "build/$name.out", where $name is the name of the task.
     */
    final Property<Object> outputDir = project.objects.property(Object)
    private Object source
    private Object srcFile
    private List<String> userPartitionArgs = []
    def String method = 'partition-users'
    def Integer sampleSize
    def Integer partitionCount
    def String outputFormat
    final Property<String> dataSetName = project.objects.property(String)
    @Deprecated
    def boolean includeTimestamps = true

    public Crossfold() {
        outputDir.set project.provider({
            "$project.buildDir/${dataSetName.get()}.out".toString()
        })
        dataSetName.set project.provider({
            getName()
        })
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

    @Deprecated
    void outputDir(Object dir) {
        logger.warn("Setting Crossfold property outputDir without assignment operator is deprecated")
        outputDir.set(dir)
    }

    @Deprecated
    void dataSetName(String name) {
        logger.warn("Setting Crossfold property dataSetName without assignment operator is deprecated")
        dataSetName.set(name)
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

    @OutputDirectory
    File getOutputDirectory() {
        return project.file(outputDir.get())
    }

    @Override
    String getCommand() {
        return 'crossfold'
    }

    @Override
    void doPrepare() {
        project.mkdir outputDirectory
    }

    @Override
    @Input
    List getCommandArgs() {
        def args = ["--output-dir", outputDirectory, "--name", dataSetName.get()]
        if (srcFile != null) {
            args << "--data-source" << project.file(srcFile)
        } else {
            project.mkdir project.buildDir
            project.file("$project.buildDir/$name-input.json").text = JsonOutput.toJson(source)
            // FIXME Don't use JSON spec
            args << "--data-source" << project.file("$project.buildDir/$name-input.json")
        }
        args << "--$method"
        args.addAll userPartitionArgs
        if (partitionCount) {
            args << '--partition-count' <<partitionCount
        }
        if (!includeTimestamps) {
            args << '--no-timestamps'
        }
        if (sampleSize != null) {
            args << '--sample-size' << sampleSize
        }
        if (outputFormat == 'gz') {
            args << '--gzip-output'
        }
        args
    }

    @Override
    File getDataSetFile() {
        return new File(getOutputDirectory(), "datasets.yaml")
    }

    /**
     * Deprecated method for user partitioning.
     * @param nop
     * @deprecated Use {@link #holdout} and friends directly.
     */
    @Deprecated
    public void userPartitionMethod(nop) {
        logger.warn('userPartitionMethod is deprecated, call holdout and friends directly')
    }

    /**
     * Specify an output format. Can be one of:
     *
     * - csv
     * - gz
     *
     * @param fmt
     */
    public void outputFormat(String fmt) {
        switch(fmt.toLowerCase()) {
        case 'csv':
            outputFormat = 'csv'
            break
        case 'csv_gz':
        case 'csv_gzip':
            logger.warn('format specification {} is deprecated, use "gz"', fmt)
        case 'gz':
            outputFormat = 'gz'
            break
        case 'pack':
            logger.error('pack output is no longer supported')
        default:
            throw new IllegalArgumentException("invalid format $fmt")
        }
    }

    /**
     * Set the method to use. Can be one of:
     * <ul>
     *     <li>partition-users</li>
     *     <li>sample-users</li>
     *     <li>partition-items</li>
     *     <li>sample-items</li>
     *     <li>partition-entities</li>
     *     <li>sample-entities</li>
     * </ul>
     * @param m The method
     */
    public void method(String m) {
        // accept partition-ratings for backwards compatibility
        if (!(m =~ /^(?i:partition[_-](users|ratings|entities|items)|sample[_-](users|items|entities))$/)) {
            throw new IllegalArgumentException("invalid partition method " + m)
        }
        method = m.replaceAll('_', '-').toLowerCase()
    }

    /**
     * Hold out a fixed number of ratings per user
     * @param n The number of ratings to hold out for each user.
     * @param order The sort order. Defaults to `random`.
     */
    public Object holdout(int n, String order = 'random') {
        userPartitionArgs = ['--holdout-count', "$n"]
        if (order == 'timestamp') {
            userPartitionArgs << '--timestamp-order'
        }
    }

    /**
     * Utility method to create a retain-N user partition method.
     * @param n The number of ratings to hold out for each user.
     * @param order The sort order. Defaults to `random`.
     */
    public Object retain(int n, String order = 'random') {
        userPartitionArgs = ['--retain', "$n"]
        if (order == 'timestamp') {
            userPartitionArgs << '--timestamp-order'
        }
    }

    /**
     * Utility method to create a holdout-fraction user partition method.
     * @param f The fraction of ratings to hold out per user.
     * @param order The sort order. Defaults to `random`.
     */
    public Object holdoutFraction(double f, String order = 'random') {
        userPartitionArgs = ['--holdout-fraction', "$f"]
        if (order == 'timestamp') {
            userPartitionArgs << '--timestamp-order'
        }
    }
}
