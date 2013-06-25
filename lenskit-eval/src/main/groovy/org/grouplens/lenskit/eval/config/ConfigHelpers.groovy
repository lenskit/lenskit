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

import org.apache.commons.lang3.builder.Builder
import org.apache.commons.lang3.reflect.ConstructorUtils
import org.apache.commons.lang3.tuple.Pair
import org.grouplens.lenskit.config.GroovyUtils
import org.grouplens.lenskit.eval.EvalTask
import org.slf4j.LoggerFactory

import static org.grouplens.lenskit.eval.config.ParameterTransforms.pickInvokable

/**
 * Helper methods for invoking configuration methods.
 * <p>
 * These methods often work by returning closures which, when invoked, will perform the
 * correct action.
 * <p>
 * <b>Warning</b>: here be dragons!
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
class ConfigHelpers {
    private static def logger = LoggerFactory.getLogger(ConfigHelpers)

    private static <T> Object makeConfigDelegate(EvalScriptEngine engine, Object target) {
        def annot = target.class.getAnnotation(ConfigDelegate)
        if (annot == null) {
            return new DefaultConfigDelegate<T>(engine, target)
        } else {
            Class<?> dlgClass = annot.value()
            // try two-arg constructor
            def ctor = dlgClass.constructors.find {
                def formals = it.parameterTypes
                formals.length == 2 && formals[0].isAssignableFrom(EvalScriptEngine) && formals[1].isInstance(target)
            }
            if (ctor != null) {
                return ctor.newInstance(engine, target)
            } else {
                ctor = dlgClass.constructors.find {
                    def formals = it.parameterTypes
                    formals.length == 1 && formals[0].isInstance(target)
                }
            }
            if (ctor != null) {
                return ctor.newInstance(target)
            } else {
                return dlgClass.newInstance()
            }
        }
    }

    /**
     * Split an array of arguments into arguments a trailing closure.
     * @param args The argument array.
     * @return A pair consisting of the arguments, except for any trailing closure, and the closure.
     * If {@var args} does not have end with a closure, {@code Pair.of(args, null)} is returned.
     */
    static Pair<Object[], Closure> splitClosure(Object[] args) {
        if (args.length > 0 && args[args.length - 1] instanceof Closure) {
            return Pair.of(Arrays.copyOf(args, args.length - 1),
                           args[args.length-1] as Closure)
        } else {
            return Pair.of(args, null)
        }
    }

    /**
     * Pick a constructor, if we can.  Returns a closure that, when invoked, constructs the object.
     * @param type The type to construct.
     * @param args The constructor arguments.
     */
    private static Closure findConstructor(Class<?> type, Object[] args) {
        def ctor = pickInvokable(args) { Class<?>[] ptypes ->
            ConstructorUtils.getMatchingAccessibleConstructor(type, ptypes)
        }
        if (ctor == null) {
            return null;
        } else {
            return {
                def txargs = ctor.right.collect({it.get()})
                ctor.left.newInstance(txargs);
            }
        }
    }

    /**
     * Resolve a method invocation with a task. Takes the name of a method and its
     * arguments and, if possible, constructs a closure that returns the result of configuring
     * a task and running it.
     * @param name The name of the method
     * @param args The arguments to the method.
     * @return A closure invoking and configuring the command, returning the built object,
     * or {@code null} if the command cannot be invoked.
     * @throws IllegalArgumentException if the command can be found but {@code args} is
     * inappropriate.
     */
    static Closure findTask(EvalScriptEngine engine, String name, Object[] args) {
        logger.debug("searching for task {}", name)
        Class<? extends EvalTask> task = engine.lookupTask(name)
        if (task == null) return null

        def command = makeTaskClosure(task, engine, args)
        if (command == null) {
            def msg = "cannot instantiate ${task.name}: no suitable constructor found"
            throw new InstantiationException(msg)
        }

        // finally have validated the arguments, move on
        return command.curry(args)
    }

    private static def makeTaskClosure(Class<? extends EvalTask> taskClass, EvalScriptEngine engine, Object[] args) {
        logger.debug("making closure for task {}", taskClass);
        def split = splitClosure(args)
        def ctor = findConstructor(taskClass, split.left)
        if (ctor != null) {
            return {
                def task = ctor.call()
                task.setEvalConfig(engine.config)
                if (split.right != null) {
                    GroovyUtils.callWithDelegate(split.right, makeConfigDelegate(engine, task))
                }
                task.call()
            }
        } else {
            return null
        }
    }

    /**
     * Resolve a method invocation with a task. Takes the name of a method and its
     * arguments and, if possible, constructs a closure that returns the result of configuring
     * a task and running it.
     * @param name The name of the method
     * @param args The arguments to the method.
     * @return A closure invoking and configuring the command, returning the built object,
     * or {@code null} if the command cannot be invoked.
     * @throws IllegalArgumentException if the command can be found but {@code args} is
     * inappropriate.
     */
    static Closure findBuilder(EvalScriptEngine engine, String name, Object[] args) {
        logger.debug("searching for builder {}", name)
        Class<? extends Builder> builderClass = engine.lookupBuilder(name)
        if (builderClass == null) return null

        def command = makeBuilderClosure(builderClass, engine, args)
        if (command == null) {
            def msg = "cannot instantiate ${builderClass.name}: no suitable constructor found"
            throw new InstantiationException(msg)
        }

        // finally have validated the arguments, move on
        return command.curry(args)
    }

    private static def makeBuilderClosure(Class<? extends Builder> builderClass, EvalScriptEngine engine, Object[] args) {
        logger.debug("making closure for task {}", builderClass);
        def split = splitClosure(args)
        def ctor = findConstructor(builderClass, split.left)
        if (ctor != null) {
            return {
                def builder = ctor.call()
                if (split.right != null) {
                    GroovyUtils.callWithDelegate(split.right, makeConfigDelegate(engine, builder))
                }
                builder.build()
            }
        } else {
            return null
        }
    }

    static <T> T constructAndConfigure(EvalScriptEngine engine, Class<T> type, Object[] args) {
        def split = splitClosure(args)
        def obj = type.metaClass.invokeConstructor(split.left)
        if (split.right != null) {
            GroovyUtils.callWithDelegate(split.right, makeConfigDelegate(engine, obj))
        }
        type.cast(obj)
    }

    /**
     * Find an external method (a builder or task) and return a closure that, when invoked, constructs
     * and configures it.  It does <strong>not</strong> invoke the builder or task, that is left up
     * to the caller.
     *
     * @param engine The script engine.
     * @param name The method name.
     * @return A closure representing the method builder, or {@code null} if no such method exists.
     */
    static Closure findExternalMethod(EvalScriptEngine engine, String name) {
        def mtype = engine.lookupMethod(name)
        if (mtype != null) {
            return {args -> constructAndConfigure(engine, mtype, args)}
        } else {
            return null
        }
    }
}
