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

import com.google.common.base.Function
import org.apache.commons.lang3.builder.Builder
import org.apache.commons.lang3.tuple.Pair
import org.grouplens.lenskit.config.GroovyUtils

import java.lang.reflect.Method

/**
 * Utilities for searching for methods of configurable objects..
 * <p>
 * <b>Warning:</b> here be dragons.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings("unchecked") // this will carry through to java stub & silence compiler
class ObjectConfiguration {
    /**
     * Search for a method with a specified BuiltBy, or a single-argument
     * method with a parameter that can be built. Used when we have a closure to
     * build a directive argument.
     * @param self The command to search.
     * @param engine The config engine.
     * @param methods The group of methods to search
     * @param args The arguments.
     * @return A closure to prepare and invoke the method, or {@code null} if no
     * such method can be found.
     * @see EvalScriptEngine#getBuilderForType(Class)
     */
    private static Closure findBuildableMethod(Object self, EvalScriptEngine engine, String name, Object[] args) {
        def oneArgMethods = self.class.methods.findAll({it.name == name && it.parameterTypes.length == 1})
        List<Closure> buildables = oneArgMethods.findResults({ Method method ->
            def param = method.parameterTypes[0]
            Class<? extends Builder> bldClass
            BuiltBy annot = method.getAnnotation(BuiltBy) ?: param.getAnnotation(BuiltBy)
            if (annot != null) {
                bldClass = annot.value()
            } else {
                bldClass = engine.getBuilderForType(param)
            }
            if (bldClass != null) {
                return {
                    def builder = constructAndConfigure(bldClass, engine, args)
                    method.invoke(self, builder.build())
                }
            } else {
                return null
            }
        })

        if (buildables.size() == 1) {
            // there is a unique builder
            return buildables[0]
        } else if (buildables.size() > 1) {
            throw new RuntimeException("multiple buildable methods named ${name}")
        } else {
            return null
        }
    }

    /**
     * Find a method that should be invoked multiple times, if the argument is iterable.
     * @param self The configurable object.
     * @param name The method name.
     * @param args The arguments.
     * @return A thunk that will invoke the method.
     */
    private static Closure findMultiMethod(Object self, String name, Object[] args) {
        if (args.length != 1) return null
        // the argument is a list
        def arg
        Class[] atypes
        arg = args[0]
        if (!(arg instanceof Iterable)) {
            return null
        }
        def type = arg.first().class
        assert type != null
        atypes = [type]
        MetaMethod mm = self.metaClass.pickMethod(name, atypes)
        if (mm != null) {
            return {
                for (elt in arg) {
                    mm.doMethodInvoke(self, elt)
                }
            }
        } else {
            return null;
        }
    }

    private static class ClosureFunction implements Function {
        private Closure closure

        ClosureFunction(Closure cl) {
            closure = cl;
        }
        def apply(input) {
            closure.call(input)
        }
    }

    /**
     * Look for a method on an object.
     * @param self The object.
     * @param name The method name.
     * @param args The method arguments.
     * @return A thunk invoking the method, or {@code null} if no such method is found.
     */
    private static Closure findMethod(Object self, String name, Object[] args) {
        // we have to get types & pick to deal with class arguments
        Class[] atypes = args.collect({it?.class})
        def mm = self.metaClass.pickMethod(name, atypes)

        // try some simple transformations
        // transform a trailing closure to a function
        if (mm == null && args.length > 0 && args.last() instanceof Closure) {
            def at2 = Arrays.copyOf(atypes, atypes.length)
            at2[args.length - 1] = Function
            mm = self.metaClass.pickMethod(name, at2)
            if (mm != null) {
                args[args.length - 1] = new ClosureFunction(args.last() as Closure)
            }
        }

        // try instantiating a single class
        if (mm == null && args.length == 1 && args.first() instanceof Class) {
            Class[] types = [args.first()]
            mm = self.metaClass.pickMethod(name, types)
            if (mm != null) {
                args = [args.first().newInstance()]
            }
        }

        mm == null ? null : {
            mm.doMethodInvoke(self, args)
        }
    }

    private static <T> Object makeConfigDelegate(Object target, EvalScriptEngine engine) {
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
     * Construct and configure a configurable object.  This instantiates the class, using the provided
     * arguments.  If the last argument is a closure, it is witheld and used to configure the object
     * after it is constructed.  No extra type coercion is performed.
     *
     * <p>If the object has an {@code evalConfig} property, that property is set to the engine's
     * configuration.
     *
     * @param type
     * @param engine
     * @param args
     * @return
     */
    private static <T> T constructAndConfigure(Class<T> type, EvalScriptEngine engine, Object[] args) {
        def split = splitClosure(args)
        def obj = type.metaClass.invokeConstructor(split.left)
        def configProp = obj.metaClass.getMetaProperty("evalConfig")
        if (configProp != null) {
            configProp.setProperty(obj, engine.config)
        }
        if (split.right != null) {
            GroovyUtils.callWithDelegate(split.right, makeConfigDelegate(obj, engine))
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
            return {args -> constructAndConfigure(mtype, engine, args)}
        } else {
            return null
        }
    }

    static def invokeConfigurationMethod(Object target, EvalScriptEngine engine,
                                         String name, Object[] args) {
        List<Closure> options = []
        def setterName = "set${name.capitalize()}"
        def adderName = "add${name.capitalize()}"
        // directly invoke
        options << {
            findMethod(target, name, args)
        }
        options << {
            findBuildableMethod(target, engine, name, args)
        }
        // invoke a setter
        options << {
            findMethod(target, setterName, args)
        }
        // invoke a buildable setter
        options << {
            findBuildableMethod(target, engine, setterName, args)
        }
        // invoke an adder
        options << {
            findMethod(target, adderName, args)
        }
        // add from a list
        options << {
            findMultiMethod(target, adderName, args)
        }
        // invoke a buildable adder
        options << {
            findBuildableMethod(target, engine, adderName, args)
        }

        def method = options.findResult({it.call()})
        if (method != null) {
            method.call()
        } else {
            // try to invoke the method directly
            target.invokeMethod(name, args)
        }
    }
}
