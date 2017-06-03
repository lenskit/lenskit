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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.lenskit.gradle.traits.DataSources

/**
 * Crossfold a data set.  This task can only crossfold a single data set; multiple tasks must be used to produce
 * multiple cross-validation splits.
 *
 * @see DataSources
 * @see <http://mooc.lenskit.org/documentation/evaluator/data/>
 */
@ParallelizableTask
class Crossfold extends LenskitTask implements DataSources, DataSetProvider {
    /**
     * The output directory for cross-validation.  Defaults to "build/$name.out", where $name is the name of the task.
     */
    def outputDir
    private Object source
    private Object srcFile
    private List<String> userPartitionArgs = []
    def String method = 'partition-users'
    def Integer sampleSize
    def Integer partitionCount
    def String outputFormat
    def String dataSetName
    @Deprecated
    def boolean includeTimestamps = true

    public Crossfold() {
        conventionMapping.outputDir = {
            "$project.buildDir/${getDataSetName()}.out"
        }
        conventionMapping.dataSetName = {
            getName()
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

    @OutputDirectory
    File getOutputDirectory() {
        return project.file(getOutputDir())
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
        def args = ["--output-dir", outputDirectory, "--name", getDataSetName()]
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
     * </ul>
     * @param m The method
     */
    public void method(String m) {
        // accept partition-ratings for backwards compatibility
        if (!(m =~ /^(?i:partition[_-](users|ratings|entities|items)|sample[_-](users|items))$/)) {
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
