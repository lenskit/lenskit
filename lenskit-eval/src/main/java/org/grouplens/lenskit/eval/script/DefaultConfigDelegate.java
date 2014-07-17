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
package org.grouplens.lenskit.eval.script;

import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Default Groovy delegate for configuring commands of evaluator components. It wraps
 * a {@link org.grouplens.lenskit.eval.EvalTask}, and dispatches methods as follows:
 * <p>
 * To resolve "foo", this delegate first looks for a method "setFoo", then "addFoo", such that one
 * of the following holds:
 * </p>
 * <ul>
 *     <li>The method takes the parameters specified.</li>
 *     <li>The parameters specified can be converted into appropriate types
 *     for the method by wrapping strings with {@code File} objects and instantiating
 *     classes with their default constructors.</li>
 *     <li>The method takes a single parameter annotated with the {@link BuiltBy}
 *     annotation. This command is constructed using a constructor that matches the arguments
 *     provided, except that the last argument is ommitted if it is a {@link groovy.lang.Closure}. If the
 *     last argument is a {@link groovy.lang.Closure}, it is used to configure the command with an appropriate
 *     delegate before the object is built.</li>
 * </ul>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public class DefaultConfigDelegate extends GroovyObjectSupport {
    protected final ConfigMethodInvoker helper;
    protected final Object target;

    /**
     * Construct a new command delegate.
     * @param target The command to use when pretending methods.
     */
    public DefaultConfigDelegate(ConfigMethodInvoker ch, Object target) {
        this.helper = ch;
        this.target = target;
    }

    public Object propertyMissing(String name) {
        return InvokerHelper.getProperty(target, name);
    }

    public void propertyMissing(String name, Object value) {
        InvokerHelper.setProperty(target, name, value);
    }

    public Object methodMissing(String name, Object args) {
        return helper.invokeConfigurationMethod(target, name,
                                                      InvokerHelper.asArray(args));
    }
}
