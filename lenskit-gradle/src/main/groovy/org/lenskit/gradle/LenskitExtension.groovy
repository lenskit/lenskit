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
/**
 * Extension for configuring LensKit.  This is registered as `lenskit` by the LensKit plugin, so you can globally
 * configure LensKit options:
 *
 * ```groovy
 * lenskit {
 *     maxMemory '10g'
 *     threadCount 16
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
 */
public class LenskitExtension {
    /**
     * The maximum number of threads LensKit should use.  Defaults to 0, which instructs LensKit to use all available
     * threads.
     */
    def int threadCount = 0

    /**
     * The maximum heap size for the LensKit JVM.
     *
     * @see org.gradle.process.JavaForkOptions#setMaxHeapSize(java.lang.String)
     */
    def String maxMemory
}
