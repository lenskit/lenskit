/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.apache.commons.lang3.builder.Builder

/**
 * Default Groovy delegate for configuring builders of evaluator components. It wraps
 * a {@link Builder}, and dispatches methods as follows:
 * <p/>
 * To resolve "foo", this delegate first looks for a method "setFoo", then "addFoo", such that one
 * of the following holds:
 * <ul>
 *     <li>The method takes the parameters specified.</li>
 *     <li>The parameters specified can be converted into appropriate types
 *     for the method by wrapping strings with {@code File} objects and instantiating
 *     classes with their default constructors.</li>
 *     <li>The method takes a single parameter annotated with the {@link DefaultBuilder}
 *     annotation. This builder is constructed using a constructor that matches the arguments
 *     provided, except that the last argument is ommitted if it is a {@link Closure}. If the
 *     last argument is a {@link Closure}, it is used to configure the builder with an appropriate
 *     delegate before the object is built.</li>
 * </ul>
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
class BuilderDelegate<T> {
    protected final EvalConfigEngine engine
    protected final Builder<T> builder

    /**
     * Construct a new builder delegate.
     * @param builder The builder to use when pretending methods.
     */
    BuilderDelegate(EvalConfigEngine engine, Builder<T> builder) {
        this.engine = engine
        this.builder = builder
    }

    def methodMissing(String name, args) {
        Closure method = null
        use (BuilderExtensions) {
            method = builder.findSetter(engine, name, args)

            if (method == null) {
                method = builder.findAdder(engine, name, args)
            }
        }

        if (method == null) {
            method = ConfigHelpers.findBuilderMethod(engine, name, args)
        }

        if (method == null) {
            // if we got this far we failed
            throw new MissingMethodException(name, builder.class, args)
        } else {
            return method.call()
        }
    }
}
