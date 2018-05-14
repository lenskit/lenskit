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

import groovy.lang.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.matcher.StringMatcherFactory;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.joda.convert.StringConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Plugin for LensKit evaluations.  This sets up the basic infrastructure required for LensKit tasks to work.
 * To use, add the following to your `build.gradle`:
 *
 * ```groovy
 * apply plugin: 'lenskit'
 * ```
 *
 * This plugin only sets up infrastructure and configuration defaults. It does *not* create any tasks.
 */
public class LenskitPlugin implements Plugin<Project> {
    public void apply(Project project) {
        final LenskitExtension lenskit = project.getExtensions().create("lenskit", LenskitExtension.class, project);

        for (MetaProperty prop : DefaultGroovyMethods.getMetaClass(lenskit).getProperties()) {
            String prjProp = "lenskit." + prop.getName();
            if (project.hasProperty(prjProp)) {
                Object val = project.findProperty(prjProp);
                String vstr = val != null ? val.toString() : null;
                logger.info("setting property {} to {}", prjProp, val);
                Class type = prop.getType();
                Consumer<Object> set = (v) -> prop.setProperty(lenskit, v);
                if (type.equals(Property.class)) {
                    Method m = null;
                    try {
                        m = lenskit.getClass().getMethod("get" + StringUtils.capitalize(prop.getName()));
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                    Type t = m.getGenericReturnType();
                    Map<TypeVariable<?>, Type> args = TypeUtils.getTypeArguments(t, Property.class);
                    Type rt = args.get(Property.class.getTypeParameters()[0]);
                    type = TypeUtils.getRawType(rt, Object.class);
                    set = (v) -> ((Property) prop.getProperty(lenskit)).set(v);
                }

                if (type.equals(List.class)) {// if the type is list update the val using strtokenizer
                    StringTokenizer tok = new StringTokenizer(vstr,
                                                              StringMatcherFactory.INSTANCE.splitMatcher(),
                                                              StringMatcherFactory.INSTANCE.quoteMatcher());
                    val = DefaultGroovyMethods.toList(tok);
                } else if (type.equals(String.class)) {
                    val = vstr;
                } else {
                    val = StringConvert.INSTANCE.convertFromString(type, vstr);
                }

                set.accept(val);
            }

        }

    }

    private static final Logger logger = LoggerFactory.getLogger(LenskitPlugin.class);
}
