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

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.StopExecutionException
import org.gradle.internal.nativeintegration.console.ConsoleDetector
import org.gradle.internal.nativeintegration.console.ConsoleMetaData
import org.gradle.internal.nativeintegration.services.NativeServices
import org.gradle.process.JavaExecSpec
import org.gradle.util.ConfigureUtil

/**
 * Base class for LensKit tasks.
 */
abstract class LenskitTask extends JavaExec {
    /**
     * Enable dry-run support. If turned on, the runner prepares but does not execute the task.
     */
    def boolean dryRun

    /**
     * The maximum memory the LensKit task should use.  Defaults to {@link LenskitExtension#getMaxMemory()}.
     */
    def String maxMemory

    /**
     * The log file.  Defaults to no log file.
     */
    def logFile

    /**
     * The output logging level.
     */
    def logLevel

    /**
     * The log file output level..
     */
    def logFileLevel

    /**
     * A Logback configuration file.  If specified, its content overrides all other logging-related options.
     */
    def logbackConfiguration

    /**
     * Whether this task depends on the files on its classpath.
     */
    def boolean dependsOnClasspath = true

    /**
     * Access the underlying invoker that will be used to run the LensKit code.  Most code will not need this; it allows
     * build scripts to set additional JVM options that are not exposed as specific properties if necessary.  Properties
     * exposed by LensKit tasks (such as {@link #getMaxMemory()}) will generally override their corresponding settings
     * in the invoker.
     */
    @Deprecated
    JavaExecSpec getInvoker() {
        return this
    }

    LenskitTask() {
        def ext = project.extensions.getByType(LenskitExtension)
        conventionMapping.jvmArgs = { ext.jvmArgs } // map jvmargs default to the jvmargs from the extension
        conventionMapping.maxMemory = { ext.maxMemory }
        conventionMapping.logLevel = { ext.logLevel }
        conventionMapping.logFileLevel = { ext.logFileLevel }
        conventionMapping.classpath = { ext.classpath ?: project.sourceSets.main.runtimeClasspath }
    }

    /**
     * Apply the LensKit JVM settings to the invoker to prepare it for running.
     */
    protected void applyFinalSettings() {
        def mem = getMaxMemory()
        if (mem != null) {
            maxHeapSize = mem
        }
        if (logbackConfiguration) {
            systemProperties 'logback.configurationFile': project.file(logbackConfiguration)
        }

        logger.info('applying JVM arguments {}', getJvmArgs())

        // the LensKit process will have stderr redirected, even if we're on a terminal
        // so we need to detect console things
        ConsoleDetector console = NativeServices.getInstance().get(ConsoleDetector.class);
        ConsoleMetaData cmd = console.getConsole();
        if (cmd?.isStdErr()) {
            // stderr is a console
            logger.info('color output, turning on color passthrough')
            systemProperties 'jansi.passthrough': true
        }
    }

    /**
     * Apply additional JVM configuration.
     * @param block The configuration block, evaluated against {@link #getInvoker()}.
     */
    @Deprecated
    public void invoker(Closure block) {
        logger.warn("invoker is deprecated, just configure the task")
        ConfigureUtil.configure(block, this)
    }

    /**
     * Method run before running the command. Used to set up (e.g. write spec files) before the command can be run.
     */
    protected void doPrepare() {}

    /**
     * Execute the LensKit task.
     */
    @Override
    void exec() {
        applyFinalSettings()
        doPrepare()
        setClasspath(getClasspath())
        logger.info 'running LensKit command {}', command
        logger.info 'Max memory: {}', getMaxMemory()
        if (getDryRun()) {
            throw new StopExecutionException()
        }
        setMain('org.lenskit.cli.Main')
        setArgs(getArgs())
        super.exec()
    }

    abstract String getCommand()
    abstract List getCommandArgs()

    @Override
    List<String> getArgs() {
        def args = []
        if (getLogFile() != null) {
            args << '--log-file' << project.file(getLogFile())
        }
        if (getLogFileLevel() != null) {
            args << '--log-file-level' << getLogFileLevel()
        }
        args.add getCommand()
        args.addAll getCommandArgs()
        return args
    }
}
