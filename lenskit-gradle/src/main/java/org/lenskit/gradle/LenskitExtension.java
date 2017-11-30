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
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.process.JavaForkOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(LenskitExtension.class);
    private final Property<Integer> threadCount;
    private final Property<String> maxMemory;
    private final ConfigurableFileCollection classpath;
    private final Property<String> logLevel;
    private final Property<String> logFileLevel;
    private final Property<List<String>> jvmArgs;

    @SuppressWarnings("unchecked")
    public LenskitExtension(Project project) {
        ObjectFactory objF = project.getObjects();
        threadCount = objF.property(Integer.class);
        threadCount.set(0);
        maxMemory = objF.property(String.class);
        classpath = project.files();
        logLevel = objF.property(String.class);
        logLevel.set("INFO");
        logFileLevel = objF.property(String.class);
        jvmArgs = objF.property((Class) List.class);
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

    /**
     * The maximum number of threads LensKit should use.  Defaults to 0, which instructs LensKit to use all available
     * threads.
     */
    public final Property<Integer> getThreadCount() {
        return threadCount;
    }

    @Deprecated
    public void threadCount(Integer tc) {
        logger.warn("DEPRECATION: LensKit property threadCount should be set with the assignment operator (=)");
        threadCount.set(tc);
    }

    /**
     * The maximum heap size for the LensKit JVM.  Defaults to `null` (no specfied heap size).
     *
     * @see JavaForkOptions#setMaxHeapSize(String)
     */
    public final Property<String> getMaxMemory() {
        return maxMemory;
    }

    @Deprecated
    public void maxMemory(String mem) {
        logger.warn("DEPRECATION: LensKit property maxMemory should be set with the assignment operator (=)");
        maxMemory.set(mem);
    }

    /**
     * The classpath to use for LensKit.  Defaults to the runtime classpath of the `main` source set.
     */
    public final ConfigurableFileCollection getClasspath() {
        return classpath;
    }

    public void classpath(Object... args) {
        classpath.setFrom(args);
    }

    /**
     * The log level to use.  Defaults to 'INFO'.
     */
    public final Property<String> getLogLevel() {
        return logLevel;
    }

    @Deprecated
    public void logLevel(String tc) {
        logger.warn("DEPRECATION: LensKit property logLevel should be set with the assignment operator (=)");
        logLevel.set(tc);
    }

    /**
     * The log level to use for log files.  Default is unset, resulting in the same level being applied to the console
     * and the log file.
     */
    public final Property<String> getLogFileLevel() {
        return logFileLevel;
    }

    @Deprecated
    public void logFileLevel(String tc) {
        logger.warn("DEPRECATION: LensKit property logFileLevel should be set with the assignment operator (=)");
        logFileLevel.set(tc);
    }

    /**
     * List of JVM arguments to use for LensKit actions.
     */
    public final Property<List<String>> getJvmArgs() {
        return jvmArgs;
    }
}
