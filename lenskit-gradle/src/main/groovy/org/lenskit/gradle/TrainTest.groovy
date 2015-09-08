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

import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.lenskit.specs.SpecUtils
import org.lenskit.specs.eval.DataSetSpec
import org.lenskit.specs.eval.TrainTestExperimentSpec

/**
 * Run a train-test evaluation.
 */
class TrainTest extends LenskitTask {
    private def spec = new TrainTestExperimentSpec()
    private def specDelegate = new SpecDelegate(spec)
    def File specFile
    private Deque<Closure<?>> deferredInput = new ArrayDeque<>()

    public TrainTest() {
        conventionMapping.specFile = {
            project.file("$project.buildDir/${name}.json")
        }
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
     * @param ds The crossfold tasks to add.
     */
    def dataSet(Crossfold cf) {
        inputs.files cf
        deferredInput << { spec ->
            def sets = SpecUtils.load(List, cf.specFile)
            sets.each {
                spec.addDataSet(it)
            }
        }
    }

    def methodMissing(String name, def args) {
        specDelegate.invokeMethod(name, args)
    }

    @Override
    String getCommand() {
        'train-test'
    }

    @InputFiles
    public void getDataInputs() {
        runDeferredTasks()
        def files = spec.dataSets.collectNested { ds ->
            ds.testSource.inputFiles + ds.trainSource.inputFiles
        }
        files.toSet()
    }

    @OutputFiles
    public void getOutputFiles() {
        spec.outputFiles
    }

    private void runDeferredTasks() {
        while (!deferredInput.isEmpty()) {
            def di = deferredInput.removeFirst()
            di.call(spec)
        }
    }

    @Override
    void doPrepare() {
        runDeferredTasks()
        def file = getSpecFile()
        project.mkdir file.parentFile
        logger.info 'preparing spec file {}', file
        SpecUtils.write(spec, file)
    }

    @Override
    List getCommandArgs() {
        def args = []
        args << getSpecFile()
    }
}
