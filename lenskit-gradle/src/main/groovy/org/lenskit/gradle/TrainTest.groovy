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

import com.google.common.io.Files
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.lenskit.gradle.delegates.DataSetSpecDelegate
import org.lenskit.gradle.delegates.EvalTaskDelegate
import org.lenskit.gradle.delegates.SpecDelegate
import org.lenskit.gradle.traits.DataSources
import org.lenskit.specs.SpecUtils
import org.lenskit.specs.eval.*

/**
 * Run a train-test evaluation.
 */
class TrainTest extends LenskitTask {
    private def spec = new TrainTestExperimentSpec()
    private def specDelegate = new SpecDelegate(project, spec)
    def File specFile
    private Deque<Closure<?>> deferredInput = new ArrayDeque<>()

    public TrainTest() {
        conventionMapping.specFile = {
            project.file("$project.buildDir/${name}.json")
        }
        spec.setThreadCount(project.lenskit.threadCount)
    }

    /**
     * Add a data set.
     * @param ds The data set to add.
     */
    void dataSet(DataSetSpec ds) {
        spec.addDataSet(ds)
    }

    /**
     * Add a data sets produced by a crossfold task.
     *
     * @param ds The crossfold tasks to add.
     */
    def dataSet(DataSetProvider cf) {
        dataSet(Collections.emptyMap(), cf)
    }

    /**
     * Configure a train-test data set.
     * @param block A block which will be used to configureSpec a {@link DataSetSpec}.
     */
    void dataSet(@DelegatesTo(DataSetSpecDelegate) Closure block) {
        dataSet SpecDelegate.configureSpec(project, DataSetSpec, DataSetSpecDelegate, block)
    }

    /**
     * Add a data sets produced by a crossfold task or other data set provider.
     *
     * <p>This method supports options for adding the crossfolded data sets:
     *
     * <dl>
     *     <dt>isolate</dt>
     *     <dd>If {@code true}, isolates each of the data sets from each other and from other data sets by assigning
     *     each a random isolation group ID.</dd>
     * </dl>
     *
     * @param options Options for adding the data sets.
     * @param ds The crossfold tasks to add.
     */
    def dataSet(Map<String,Object> options, DataSetProvider cf) {
        inputs.files cf
        deferredInput << { spec ->
            cf.dataSets.each {
                def dss = SpecUtils.copySpec(it)
                if (options.isolate) {
                    dss.isolationGroup = UUID.randomUUID()
                }
                spec.addDataSet(dss)
            }
        }
    }

    /**
     * Load one or more algorithms from a file.
     * @param name The algorithm name.
     * @param file The file.
     */
    void algorithm(String name, file) {
        def aspec = new AlgorithmSpec()
        def theFile = project.file(file)
        aspec.name = name ?: Files.getNameWithoutExtension(theFile.name)
        aspec.configFile = theFile.toPath()
        spec.addAlgorithm(aspec)
    }

    /**
     * Load one or more algorithms from a file.
     * @param file The algorithm file
     */
    void algorithm(file) {
        algorithm(null, file)
    }

    /**
     * Configure a prediction task.
     * @param block The block.
     * @see PredictEvalTaskSpec
     */
    void predict(@DelegatesTo(EvalTaskDelegate) Closure block) {
        def task = SpecDelegate.configureSpec(project, PredictEvalTaskSpec, EvalTaskDelegate, block)
        spec.addTask(task)
    }

    /**
     * Configure a prediction task.
     * @param block The block.
     * @see PredictEvalTaskSpec
     */
    void recommend(@DelegatesTo(EvalTaskDelegate) Closure block) {
        def task = SpecDelegate.configureSpec(project, RecommendEvalTaskSpec, EvalTaskDelegate, block)
        spec.addTask(task)
    }

    def methodMissing(String name, def args) {
        specDelegate.invokeMethod(name, args)
    }

    @Override
    String getCommand() {
        'train-test'
    }

    @InputFiles
    public Set<File> getDataInputs() {
        def fspec = getFinalSpec()
        Set<File> files = new HashSet<>()
        for (ds in fspec.dataSets) {
            files.addAll(ds.testSource.inputFiles*.toFile())
            files.addAll(ds.trainSource.inputFiles*.toFile())
        }
        return files
    }

    @InputFiles
    public Set<File> getConfigInputs() {
        return spec.algorithms*.configFile*.toFile().toSet()
    }

    @OutputFiles
    public Set<File> getOutputFiles() {
        spec.outputFiles*.toFile()
    }

    private TrainTestExperimentSpec getFinalSpec() {
        def copy = SpecUtils.copySpec(spec)
        for (func in deferredInput) {
            func.call(copy)
        }
        return copy
    }

    @Override
    void doPrepare() {
        def fspec = getFinalSpec()
        def file = getSpecFile()
        project.mkdir file.parentFile
        logger.info 'preparing spec file {}', file
        SpecUtils.write(fspec, file.toPath())
    }

    @Override
    List getCommandArgs() {
        def args = []
        args << getSpecFile()
    }
}
