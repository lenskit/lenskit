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
