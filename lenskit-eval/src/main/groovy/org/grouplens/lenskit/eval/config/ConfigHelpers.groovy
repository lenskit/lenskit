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
import org.grouplens.lenskit.eval.EvalTask
import static org.grouplens.lenskit.eval.config.ParameterTransforms.pickInvokable
import org.apache.commons.lang3.reflect.ConstructorUtils

/**
 * Helper methods for invoking configuration methods.
 * @author Michael Ekstrand
 * @since 0.10
 */
class ConfigHelpers {
    static <T> Object makeBuilderDelegate(EvalConfigEngine engine, Builder<T> builder) {
        def annot = builder.class.getAnnotation(ConfigDelegate)
        if (annot == null) {
            return new BuilderDelegate<T>(engine, builder)
        } else {
            Class<?> dlgClass = annot.value()
            // try two-arg constructor
            def ctor = dlgClass.constructors.find {
                def formals = it.parameterTypes
                formals.length == 2 && formals[0].isAssignableFrom(EvalConfigEngine) && formals[1].isInstance(builder)
            }
            if (ctor != null) {
                return ctor.newInstance(engine, builder)
            } else {
                ctor = dlgClass.constructors.find {
                    def formals = it.parameterTypes
                    formals.length == 1 && formals[0].isInstance(builder)
                }
            }
            if (ctor != null) {
                return ctor.newInstance(builder)
            } else {
                return dlgClass.newInstance()
            }
        }
    }

    /**
     * Invoke a builder to configure an object.
     * @param builder The builder to use.
     * @param closure A closure to configure the builder. Can be {@code null}. If non-null,
     * this closure is invoked with a {@link BuilderDelegate} to configure the builder.
     * @return The object resulting from the builder.
     */
    static <T> T invokeBuilder(EvalConfigEngine engine, Builder<T> builder, Closure closure) {
        if (closure != null) {
            def delegate = makeBuilderDelegate(engine, builder)
            closure.setDelegate(delegate)
            closure.setResolveStrategy(Closure.DELEGATE_FIRST)
            closure.run()
        }
        builder.build()
    }

    /**
     * Resolve a method invocation with a builder factory. Takes the name of a method and its
     * arguments and, if possible, constructs a closure that returns the result of configuring
     * a builder and running it.
     * @param name The name of the method
     * @param args The arguments to the method.
     * @return A closure invoking and configuring the builder, returning the built object,
     * or {@code null} if the builder cannot be invoked.
     * @throws IllegalArgumentException if the builder can be found but {@code args} is
     * inappropriate.
     */
    static Closure findBuilderMethod(EvalConfigEngine engine, String name, args) {
        Class<? extends Builder> builderClass = engine.getBuilder(name)
        if (builderClass == null) return null

        def build = makeBuilderClosure(builderClass, engine, args)
        if (build == null) {
            def msg = "cannot instantiate ${builderClass.name}: no suitable constructor found"
            throw new InstantiationException(msg)
        }

        // finally have validated the arguments, move on
        return {
            def obj = build(args)
            if (obj instanceof EvalTask) {
                engine.registerTask(obj as EvalTask)
            }
            obj
        }
    }

    static def makeBuilderClosure(Class<? extends Builder> bld, EvalConfigEngine engine, Object[] args) {
        Closure block = null
        Object[] trimmedArgs
        if (args.length > 0 && args[args.length-1] instanceof Closure) {
            block = args[args.length-1] as Closure
            trimmedArgs = Arrays.copyOf(args, args.length-1)
        } else {
            trimmedArgs = args
        }
        def bestCtor = pickInvokable(trimmedArgs) {
            ConstructorUtils.getMatchingAccessibleConstructor(bld, it)
        }
        if (bestCtor != null) {
            return {
                Object[] txargs = bestCtor.right.collect({it.get()})
                def builder = bestCtor.left.newInstance(txargs)
                ConfigHelpers.invokeBuilder(engine, builder, block)
            }
        } else {
            return null
        }
    }
}
