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

import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec
import org.gradle.process.internal.JavaExecHandleBuilder
import org.gradle.util.ConfigureUtil;

/**
 * Base class for LensKit tasks.
 */
public abstract class LenskitTask extends ConventionTask {
    /**
     * The maximum memory the LensKit task should use.  Defaults to {@link LenskitExtension#getMaxMemory()}.
     */
    def String maxMemory

    /**
     * The classpath to use.
     */
    def FileCollection classpath

    /**
     * Access the underlying invoker that will be used to run the LensKit code.  Most code will not need this; it allows
     * build scripts to set additional JVM options that are not exposed as specific properties if necessary.  Properties
     * exposed by LensKit tasks (such as {@link #getMaxMemory()}) will generally override their corresponding settings
     * in the invoker.
     */
    final def JavaExecSpec invoker

    LenskitTask() {
        invoker = new JavaExecHandleBuilder(services.get(FileResolver))
        def ext = project.extensions.getByType(LenskitExtension)
        conventionMapping.maxMemory = { ext.maxMemory }
        // FIXME Make dependencies work!
        conventionMapping.classpath = { ext.classpath ?: project.sourceSets.main.runtimeClasspath }
    }

    /**
     * Apply the LensKit JVM settings to the invoker to prepare it for running.
     */
    protected void applyFinalSettings() {
        def mem = getMaxMemory()
        if (mem != null) {
            invoker.maxHeapSize = mem
        }
        if (getClasspath() != null) {
            invoker.classpath = getClasspath()
        }
    }

    /**
     * Apply additional JVM configuration.
     * @param block The configuration block, evaluated against {@link #getInvoker()}.
     */
    public void invoker(Closure block) {
        ConfigureUtil.configure(block, invoker)
    }

    /**
     * Method run before running the command. Used to set up (e.g. write spec files) before the command can be run.
     */
    protected void doPrepare() {}

    /**
     * Method run after running the command.  It is run if there is a failure running the LensKit command, but not
     * if the setup for the command ({@link #doPrepare()}, {@link #getCommandArgs()}, etc.) fail.
     * @param The result, or `null` if the command could not be executed at all.
     */
    protected void doCleanup(ExecResult result) {}

    /**
     * Execute the LensKit task.
     */
    @TaskAction
    public void perform() {

        logger.info 'running LensKit command {}', command
        logger.info 'Max memory: {}', maxMemory
        applyFinalSettings()
        invoker.main = 'org.lenskit.cli.Main'
        invoker.args command
        invoker.args commandArgs
        def bld = invoker as JavaExecHandleBuilder
        ExecResult result = null
        try {
            result = bld.build().start().waitForFinish()
        } finally {
            doCleanup(result)
        }
        result.assertNormalExitValue()
    }

    abstract String getCommand();
    abstract List getCommandArgs();
}
