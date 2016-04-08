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

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.lenskit.gradle.delegates.SpecDelegate
import org.lenskit.gradle.traits.DataBuilder
import org.lenskit.gradle.traits.DataSources
import org.lenskit.specs.SpecUtils
import org.lenskit.specs.data.DataSourceSpec
import org.lenskit.specs.data.TextDataSourceSpec
import org.lenskit.specs.eval.CrossfoldSpec
import org.lenskit.specs.eval.DataSetSpec
import org.lenskit.specs.eval.PartitionMethodSpec

import java.nio.file.Path

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
 * partitions 10
 * </code></pre>
 *
 * @see CrossfoldSpec
 * @see DataSources
 * @see http://mooc.lenskit.org/documentation/evaluator/data/
 */
class Crossfold extends LenskitTask implements DataSources, DataSetProvider {
    private def spec = new CrossfoldSpec();
    private def specDelegate = new SpecDelegate(project, spec)

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
     * Set the input source.
     * @param src
     */
    void input(DataSourceSpec src) {
        spec.source = src
    }

    /**
     * Set the input source.
     * @param bld The input source.
     */
    void input(DataBuilder bld) {
        dependsOn bld
        spec.deferredSource = bld.deferredDataSourceSpec
    }

    /**
     * Configure an input CSV file of ratings.  Convenience method; {@link #input(DataSourceSpec)} is more general.
     * @param csv A CSV file containing ratings.
     */
    void inputFile(File csv) {
        def src = new TextDataSourceSpec()
        src.delimiter = ","
        src.file = csv.toPath()
        input src
    }

    def methodMissing(String name, def args) {
        specDelegate.invokeMethod(name, args)
    }

    @InputFiles
    Set<File> getInputFiles() {
        return spec.source?.inputFiles?.collect {
            it.toFile()
        } ?: []
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
        spec.name = name
        SpecUtils.write(spec, specFile)
    }

    @Override
    List getCommandArgs() {
        def args = []
        args << '-o'
        args << project.file(getOutputDir())
        args << specFile
    }

    Path getSpecFile() {
        return project.file(getOutputDir()).toPath().resolve("crossfold.json")
    }

    List<DataSetSpec> getDataSets() {
        def copy = SpecUtils.copySpec(spec)
        copy.setOutputDir(project.file(getOutputDir()).toPath())
        return copy.dataSets
    }

    /**
     * Utility method to create a holdout-N user partition method.
     * @param n The number of ratings to hold out for each user.
     * @param order The sort order. Defaults to `random`.
     * @return The partition method.
     */
    public static PartitionMethodSpec holdout(int n, String order = 'random') {
        def spec = new PartitionMethodSpec.Holdout()
        spec.count = n
        spec.order = order
        spec
    }

    public PartitionMethodSpec holdout(Closure block) {
        SpecDelegate.configureSpec(project, PartitionMethodSpec.Holdout, block)
    }

    /**
     * Utility method to create a retain-N user partition method.
     * @param n The number of ratings to hold out for each user.
     * @param order The sort order. Defaults to `random`.
     * @return The partition method.
     */
    public PartitionMethodSpec retain(int n, String order = 'random') {
        def spec = new PartitionMethodSpec.Retain()
        spec.count = n
        spec.order = order
        spec
    }

    public PartitionMethodSpec retain(Closure block) {
        SpecDelegate.configureSpec(project, PartitionMethodSpec.Retain, block)
    }

    /**
     * Utility method to create a holdout-fraction user partition method.
     * @param f The fraction of ratings to hold out per user.
     * @param order The sort order. Defaults to `random`.
     * @return The partition method.
     */
    public PartitionMethodSpec holdoutFraction(double f, String order = 'random') {
        def spec = new PartitionMethodSpec.HoldoutFraction()
        spec.fraction = f
        spec.order = order
        spec
    }

    public PartitionMethodSpec holdoutFraction(Closure block) {
        SpecDelegate.configureSpec(project, PartitionMethodSpec.HoldoutFraction, block)
    }
}
