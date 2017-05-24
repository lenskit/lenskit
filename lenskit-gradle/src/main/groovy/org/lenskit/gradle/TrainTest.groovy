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

import com.google.common.io.Files
import groovy.json.JsonOutput
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.util.ConfigureUtil
import org.lenskit.gradle.delegates.DataSetConfig
import org.lenskit.gradle.delegates.EvalTaskConfig
import org.lenskit.gradle.delegates.RecommendEvalTaskConfig
import org.lenskit.gradle.traits.GradleUtils

import java.util.concurrent.Callable

/**
 * Run a train-test evaluation.
 */
@ParallelizableTask
class TrainTest extends LenskitTask implements GradleUtils {
    /**
     * The output file for recommendation output.
     */
    def outputFile

    /**
     * The user output file for recommendation output.
     */
    def userOutputFile

    /**
     * The cache directory for the recommender.
     */
    def cacheDirectory

    /**
     * The thread count for the evaluator.
     */
    def int threadCount

    /**
     * Then number of parallel tasks to allow.
     */
    def int parallelTasks = 0

    /**
     * Configure whether the evaluator should share model components between algorithms.
     */
    def boolean shareModelComponents = true

    /**
     * Configure whether the evaluation will continue after errors.
     */
    def boolean continueAfterError = false

    private Map<String,Object> algorithms = new HashMap<>()
    private List<Callable> dataSets = []
    private List<EvalTaskConfig> evalTasks = []

    /**
     * The file name for writing out the experiment description. Do not change unless absolutely necessary.
     */
    def File specFile

    public TrainTest() {
        conventionMapping.threadCount = {
            project.lenskit.threadCount
        }
        conventionMapping.outputFile = {
            "$project.buildDir/${name}.csv"
        }
        conventionMapping.specFile = {
            project.file("$project.buildDir/${name}-spec.json")
        }
    }

    /**
     * Add a data set.
     * @param ds The data set configuration to add.
     */
    void dataSet(Map ds) {
        dataSets.add {ds}
    }

    /**
     * Add a data set.
     * @param ds The file of the data set to add.
     */
    void dataSet(Object ds) {
        inputs.file ds
        dataSets.add({makeUrl(ds, getSpecFile())})
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
     * @param block A block which will be used to configure a data set.
     */
    void dataSet(@DelegatesTo(DataSetConfig) Closure block) {
        def set = new DataSetConfig(project)
        ConfigureUtil.configure(block, set)
        dataSets.add({[name: set.name,
                       test: makeUrl(set.testSource, getSpecFile()),
                       train: makeUrl(set.trainSource, getSpecFile())]})
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
        if (options.isolate) {
            throw new UnsupportedOperationException("isolation not currently supported")
        } else {
            dataSets.add {
                makeUrl(cf.dataSetFile, getSpecFile())
            }
        }
    }

    /**
     * Load one or more algorithms from a file.
     * @param name The algorithm name.
     * @param file The file.
     */
    void algorithm(String name, file) {
        algorithms[name ?: Files.getNameWithoutExtension(file)] = file
        inputs.file file
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
     */
    void predict(@DelegatesTo(EvalTaskConfig) Closure block) {
        def task = new EvalTaskConfig(project, 'predict')
        task.configure block
        evalTasks.add(task)
    }

    /**
     * Configure a top-N recommendation task.
     * @param block The block.
     */
    void recommend(@DelegatesTo(RecommendEvalTaskConfig) Closure block) {
        def task = new RecommendEvalTaskConfig(project)
        task.configure block
        evalTasks.add(task)
    }

    /**
     * Configure a rank effectiveness task.  This is built on {@link #recommend(Closure)}, but sets options to defaults
     * that make the resulting task a rank effectiveness measure when used with suitable metrics such as NDCG.
     *
     * @param block The configuration block.
     */
    void rank(@DelegatesTo(RecommendEvalTaskConfig) Closure block) {
        def task = new RecommendEvalTaskConfig(project)
        task.labelPrefix = 'Rank'
        task.candidates = 'user.testItems'
        task.listSize = -1
        task.configure block
        evalTasks.add(task)
    }

    @Input
    def getJson() {
        def json = [output_file           : makeUrl(getOutputFile(), getSpecFile()),
                    user_output_file      : makeUrl(getUserOutputFile(), getSpecFile()),
                    cache_directory       : makeUrl(getCacheDirectory(), getSpecFile()),
                    thread_count          : getThreadCount(),
                    parallel_tasks        : getParallelTasks(),
                    share_model_components: getShareModelComponents(),
                    continue_after_error  : getContinueAfterError()]
        json.datasets = dataSets.collect {it.call()}
        json.algorithms = algorithms.collectEntries {k, v ->
            [k, makeUrl(v, getSpecFile())]
        }
        json.tasks = evalTasks.collect({it.json})

        return json
    }

    @Override
    String getCommand() {
        'train-test'
    }

    @OutputFiles
    public Set<File> getOutputFiles() {
        Set files = [outputFile, userOutputFile].findAll().collect { project.file(it) }
        files.addAll(evalTasks.collect({it.outputFile}).findAll().collect {
            project.file(it)
        })
        return files
    }

    @Override
    void doPrepare() {
        def file = getSpecFile()
        project.mkdir file.parentFile
        logger.info 'preparing spec file {}', file
        file.text = JsonOutput.prettyPrint(JsonOutput.toJson(json))
    }

    @Override
    List getCommandArgs() {
        def args = []
        args << getSpecFile()
    }
}
