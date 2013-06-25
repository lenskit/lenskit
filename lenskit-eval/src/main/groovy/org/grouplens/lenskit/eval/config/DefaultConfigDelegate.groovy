/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.config
/**
 * Default Groovy delegate for configuring commands of evaluator components. It wraps
 * a {@link org.grouplens.lenskit.eval.EvalTask}, and dispatches methods as follows:
 * <p/>
 * To resolve "foo", this delegate first looks for a method "setFoo", then "addFoo", such that one
 * of the following holds:
 * <ul>
 *     <li>The method takes the parameters specified.</li>
 *     <li>The parameters specified can be converted into appropriate types
 *     for the method by wrapping strings with {@code File} objects and instantiating
 *     classes with their default constructors.</li>
 *     <li>The method takes a single parameter annotated with the {@link BuiltBy}
 *     annotation. This command is constructed using a constructor that matches the arguments
 *     provided, except that the last argument is ommitted if it is a {@link Closure}. If the
 *     last argument is a {@link Closure}, it is used to configure the command with an appropriate
 *     delegate before the object is built.</li>
 * </ul>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
class DefaultConfigDelegate<T> {
    protected final EvalScriptEngine engine
    protected final Object target

    /**
     * Construct a new command delegate.
     * @param target The command to use when pretending methods.
     */
    DefaultConfigDelegate(EvalScriptEngine engine, Object target) {
        this.engine = engine
        this.target = target
    }

    def propertyMissing(String name) {
        target.getMetaClass().getProperty(target, name)
    }

    def methodMissing(String name, args) {
        Closure method = null
        use(ConfigurableExtensions) {
            method = target.findSetter(engine, name, args)

            if (method == null) {
                method = target.findAdder(engine, name, args)
            }
        }

        if (method == null) {
            // if we got this far we failed
            throw new MissingMethodException(name, target.class, args)
        } else {
            return method.call()
        }
    }
}
