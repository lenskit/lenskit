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

import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFiles

class Simulate extends LenskitTask {
    def File specFile

    Simulate() {
        conventionMapping.specFile = {
            project.file("$project.buildDir/${name}.json")
        }
        spec.outputFile = project.file("$project.buildDir/${name}-output.csv").toPath()
        // FIXME re-enable simulate task
        throw new UnsupportedOperationException("simulate task does not work")
    }

    /**
     * Specify the input file.
     * @param obj The name of the input file.
     */
    public void input(obj) {
        spec.inputFile = project.file(obj).toPath()
    }

    /**
     * Get the input file for the simulation.
     * @return The simulation input file.
     */
    @InputFile
    public File getInputFile() {
        return spec.inputFile?.toFile()
    }

    /**
     * Set the output file.
     * @param obj The output file.
     */
    public void output(obj) {
        spec.outputFile = project.file(obj).toPath()
    }

    /**
     * Get the primary output file for the simulation.
     * @return The CSV output file for the simulation.
     */
    public File getOutputFile() {
        return spec.outputFile?.toFile()
    }

    /**
     * Set the extended output file.
     * @param obj The extended output file.
     */
    public void extendedOutput(obj) {
        spec.extendedOutputFile = project.file(obj).toPath()
    }

    /**
     * Get the output file for the extended JSON output.
     * @return The file where the extended JSON output will be stored.
     */
    public File getExtendedOutputFile() {
        return spec.extendedOutputFile?.toFile()
    }

    public void algorithm(fn) {
         algorithm(null, fn)
    }

    public void algorithm(name, fn) {
        def file = project.file(fn)
        if (name == null) {
            name = file.name
        }
        // FIXME fix this
    }

    @OutputFiles
    public Set<File> getOutputFiles() {
        return spec.outputFiles*.toFile()
    }

    public void listSize(int s) {
        spec.listSize = s
    }

    public int setListSize(int s) {
        spec.listSize = s
    }

    public int getListSize() {
        return spec.listSize
    }

    public void rebuildPeriod(long p) {
        spec.rebuildPeriod = p
    }

    public void setRebuildPeriod(long p) {
        spec.rebuildPeriod = p
    }

    public long getRebuildPeriod() {
        return spec.rebuildPeriod
    }

    @Override
    protected void doPrepare() {
        def file = getSpecFile()
        project.mkdir file.parentFile
        logger.info('preparing spec file {}', file)
    }

    @Override
    String getCommand() {
        return "simulate"
    }

    @Override
    List getCommandArgs() {
        return ["--spec-file", getSpecFile()]
    }
}
