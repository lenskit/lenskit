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
package org.lenskit.gradle;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.PropertyState;
import org.gradle.process.JavaForkOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extension for configuring LensKit.  This is registered as `lenskit` by the LensKit plugin, so you can globally
 * configureSpec LensKit options:
 *
 * ```groovy
 * lenskit {
 * maxMemory '10g'
 * threadCount 16
 * }
 * ```
 *
 * Each property has a default value; the LensKit plugin also examines the project properties for properties starting
 * with `lenskit.` to initialize the extension properties.  This allows you to override property defaults on the
 * command line:
 *
 * ```
 * ./gradlew evaluate -Plenskit.threadCount=10
 * ```
 *
 * @see http://mooc.lenskit.org/documentation/evaluator/gradle/
 */
public class LenskitExtension {
    @SuppressWarnings("unchecked")
    public LenskitExtension(Project project) {
        threadCount = project.property(Integer.class);
        threadCount.set(0);
        maxMemory = project.property(String.class);
        classpath = project.files();
        logLevel = project.property(String.class);
        logLevel.set("INFO");
        logFileLevel = project.property(String.class);
        jvmArgs = project.property((Class) List.class);
    }

    /**
     * Add JVM arguments for LensKit tasks.
     *
     * @param val JVM arguments to add. They are appended to the current list of arguments.
     */
    public void jvmArgs(String... val) {
        List<String> list = jvmArgs.getOrNull();
        if (list == null) {
            list = new ArrayList<>();
        }
        jvmArgs.set(DefaultGroovyMethods.plus(list, Arrays.asList(val)));
    }

    public final PropertyState<Integer> getThreadCount() {
        return threadCount;
    }

    public final PropertyState<String> getMaxMemory() {
        return maxMemory;
    }

    public final ConfigurableFileCollection getClasspath() {
        return classpath;
    }

    public final PropertyState<String> getLogLevel() {
        return logLevel;
    }

    public final PropertyState<String> getLogFileLevel() {
        return logFileLevel;
    }

    public final PropertyState<List<String>> getJvmArgs() {
        return jvmArgs;
    }

    /**
     * The maximum number of threads LensKit should use.  Defaults to 0, which instructs LensKit to use all available
     * threads.
     */
    private final PropertyState<Integer> threadCount;
    /**
     * The maximum heap size for the LensKit JVM.  Defaults to `null` (no specfied heap size).
     *
     * @see JavaForkOptions#setMaxHeapSize(String)
     */
    private final PropertyState<String> maxMemory;
    /**
     * The classpath to use for LensKit.  Defaults to the runtime classpath of the `main` source set.
     */
    private final ConfigurableFileCollection classpath;
    /**
     * The log level to use.  Defaults to 'INFO'.
     */
    private final PropertyState<String> logLevel;
    /**
     * The log level to use for log files.  Default is unset, resulting in the same level being applied to the console
     * and the log file.
     */
    private final PropertyState<String> logFileLevel;
    /**
     * List of JVM arguments to use for LensKit actions.
     */
    private final PropertyState<List<String>> jvmArgs;
}
