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

import org.apache.commons.lang3.text.StrMatcher
import org.apache.commons.lang3.text.StrTokenizer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.joda.convert.StringConvert
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Plugin for LensKit evaluations.  This sets up the basic infrastructure required for LensKit tasks to work.
 * To use, add the following to your `build.gradle`:
 *
 * ```groovy
 * apply plugin: 'lenskit'
 * ```
 *
 * This task only sets up infrastructure and configuration defaults. It does *not* create any tasks.
 *
 * @see http://mooc.lenskit.org/documentation/evaluator/gradle/
 */
public class LenskitPlugin implements Plugin<Project> {
    private static final Logger logger = LoggerFactory.getLogger(LenskitPlugin.class);

    public void apply(Project project) {
        def lenskit = project.extensions.create("lenskit", LenskitExtension)

        for (prop in lenskit.metaClass.properties) {
            def prjProp = "lenskit.$prop.name"
            if (project.hasProperty(prjProp)) {
                def val = project.getProperty(prjProp)
                logger.info 'setting property {} to {}', prjProp, val
                if (prop.type == List) { // if the type is list update the val using strtokenizer
                    StrTokenizer tok = new StrTokenizer(val as String,
                                                        StrMatcher.splitMatcher(),
                                                        StrMatcher.quoteMatcher());
                    val = tok.toList()
                } else if (prop.type != String) {
                    val = StringConvert.INSTANCE.convertFromString(prop.type, val)
                }
                prop.setProperty(lenskit, val)
            }
        }
    }
}
