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

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.lenskit.gradle.traits.DataBuilder
import org.lenskit.gradle.traits.DataSources
import org.lenskit.specs.SpecUtils
import org.lenskit.specs.data.DataSourceSpec
import org.lenskit.specs.data.TextDataSourceSpec
import org.lenskit.specs.eval.CrossfoldSpec
import org.lenskit.specs.eval.DataSetSpec

import java.util.concurrent.Callable

/**
 * Crossfold a data set.  This task can only crossfold a single data set; multiple tasks must be used to produce
 * multiple cross-validation splits.
 *
 * In addition to the methods and properties specified in this class, the crossfolder also supports all configuration
 * directives supported by the crossfold operation as defined by {@link CrossfoldSpec}.
 * For example, you can say:
 *
 * <pre><code class="groovy">
 * includeTimestamps false
 * partitionCount 10
 * </code></pre>
 *
 * @see CrossfoldSpec
 * @see DataSources
 * @see <http://mooc.lenskit.org/documentation/evaluator/data/>
 */
class Crossfold extends LenskitTask implements DataSources, DataSetProvider {
    /**
     * The output directory for cross-validation.  Defaults to "build/$name.out", where $name is the name of the task.
     */
    def outputDir
    private Callable<DataSourceSpec> source
    private Object srcFile
    private List<String> userPartitionArgs = []
    def String method = 'partition-users'
    def Integer sampleSize
    def Integer partitionCount
    def String outputFormat
    @Deprecated
    def boolean useTimestamps = true

    public Crossfold() {
        conventionMapping.outputDir = {
            "$project.buildDir/${name}.out"
        }
    }

    /**
     * Set the data set name.
     * @param name The data set name.
     */
    void dataSetName(String name) {
        spec.name = name;
    }

    /**
     * Set the data set name.
     * @param name The data set name.
     */
    void setDataSetName(String name) {
        spec.name = name
    }

    /**
     * Get the data set name.
     * @return The data set name.
     */
    String getDataSetName() {
        return spec.name
    }

    /**
     * Set the input source.
     * @param src
     */
    void input(DataSourceSpec src) {
        source = {src}
    }

    /**
     * Set the input source.
     * @param bld The input source.
     */
    void input(DataBuilder bld) {
        dependsOn bld
        source = {bld.deferredDataSourceSpec.get()}
    }

    /**
     * Set the input source manifest.
     * @param file The path to an input source manifest file (in YAML format).
     */
    void input(Object file) {
        srcFile = file
    }

    /**
     * Configure an input CSV file of ratings.  Convenience method; {@link #input(DataSourceSpec)} is more general.
     * @param csv A CSV file containing ratings.
     */
    void inputFile(Object csv) {
        source = {
            def src = new TextDataSourceSpec()
            src.delimiter = ","
            src.file = project.file(csv).toPath()
            src
        }
    }

    @InputFiles
    Set<File> getInputFiles() {
        return source?.call()?.inputFiles?.collect {
            it.toFile()
        } ?: []
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
        def args = ["--output-dir", outputDirectory]
        if (srcFile != null) {
            args << "--data-source" << project.file(srcFile)
        } else {
            project.file("$project.buildDir/$name-input.json").text = SpecUtils.stringify(source.call())
            // FIXME Don't use JSON spec
            args << "--data-source" << project.file("$project.buildDir/$name-input.json")
        }
        args << "--$method"
        args.addAll userPartitionArgs
        if (partitionCount) {
            args << '--partition-count' <<partitionCount
        }
        if (!useTimestamps) {
            args << '--no-timestamps'
        }
        if (sampleSize != null) {
            args << '--sample-size' << sampleSize
        }
        if (outputFormat == 'gz') {
            args << '--gzip-output'
        } else if (outputFormat == 'pack') {
            args << '--pack-output'
        }
        args
    }

    List<DataSetSpec> getDataSets() {
        return finalSpec.dataSets
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
        case 'gz':
            outputFormat = 'gz'
            break
        case 'pack':
            logger.warn('pack output is deprecated')
            outputFormat = 'pack'
            break
        default:
            throw new IllegalArgumentException("invalid format $fmt")
        }
    }

    /**
     * Set the method to use. Can be one of:
     * <ul>
     *     <li>partition-users</li>
     *     <li>partition-ratings</li>
     *     <li>sample-users</li>
     * </ul>
     * @param m The method
     */
    public void method(String m) {
        if (!(m =~ /^(?i:partition[_-](users|ratings)|sample[_-]users)$/)) {
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
