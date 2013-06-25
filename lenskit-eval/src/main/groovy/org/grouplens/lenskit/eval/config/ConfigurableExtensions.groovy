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

import java.lang.reflect.Method

import org.slf4j.LoggerFactory
import static ParameterTransforms.pickInvokable

/**
 * Utilities for searching for methods of configurable objects..
 * <p>
 * <b>Warning:</b> here be dragons.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings("unchecked") // this will carry through to java stub & silence compiler
class ConfigurableExtensions {
    private static final def logger = LoggerFactory.getLogger(ConfigurableExtensions)

    /**
     * Find a method compatible with some arguments.
     * @param self The command.
     * @param name The method name.
     * @param args The arguments.
     * @return A no-argument closure invoking the method, or {@code null}.
     */
    static def findMethod(Object self, String name, Object[] args) {
        logger.debug("searching for method {}", name)
        def atypes = new Class[args.length]
        for (i in 0..<args.length) {
            atypes[i] = args[i].class
        }

        // try to just get a matching method
        MetaMethod mm = self.metaClass.pickMethod(name, atypes)
        if (mm != null) {
            logger.debug("found method {}", mm)
            return {
                mm.doMethodInvoke(self, args)
            }
        }

        // try to pick a method based on basic transformations
        def inv = pickInvokable(args) {self.metaClass.pickMethod(name, it)}
        if (inv == null) {
            return null
        } else {
            return {
                Object[] txargs = inv.right.collect({it.get()})
                inv.left.invoke(self, txargs)
            }
        }
    }

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
    static def findBuildableMethod(Object self, EvalScriptEngine engine, List<Method> methods, Object[] args) {
        def oneArgMethods = methods.findAll({it.parameterTypes.length == 1})
        def buildables = oneArgMethods.collect({ method ->
            def param = method.parameterTypes[0]
            Class<? extends Builder> bldClass = null
            BuiltBy annot = method.getAnnotation(BuiltBy) ?: param.getAnnotation(BuiltBy)
            if (annot != null) {
                bldClass = annot.value()
            } else {
                bldClass = engine.getBuilderForType(param)
            }
            if (bldClass != null) {
                return {
                    def builder = ConfigHelpers.constructAndConfigure(engine, bldClass, args)
                    method.invoke(self, builder.build())
                }
            } else {
                return null
            }
        }).findAll()

        if (buildables.size() == 1) {
            // there is a unique builder
            return buildables.get(0)
        } else if (buildables.size() > 1) {
            throw new RuntimeException("too many buildable options")
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
    static def findMultiMethod(Object self, String name, Object[] args) {
        if (args.length != 1) return null
        // the argument is a list
        def arg
        Class[] atypes
        arg = args[0]
        if (!List.class.isAssignableFrom(arg.class)) {
            return null
        }
        def type = arg[0].class
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

    static List<Method> getMethods(Object self, String name) {
        self.class.methods.findAll {it.name == name}
    }

    /**
     * Find a setter method compatible with a specific property name and arguments.
     * @param self The command.
     * @param name The property name.
     * @param args The arguments.
     * @return A no-argument closure that either invokes the method if it is found
     * or throws an exception if there is no matching method.
     */
    static def findSetter(Object self, EvalScriptEngine engine, String name, Object... args) {
        name = "set" + name.capitalize()
        def methods = getMethods(self, name)

        if (args.length == 1 && args[0] == null) {
            if (methods.size() == 1) {
                def method = methods[0]
                def formals = method.parameterTypes
                if (formals.size() == 1 && !formals[0].isPrimitive()) {
                    return {
                        method.invoke(self, args)
                    }
                } else {
                    return {
                        throw new IllegalArgumentException("multiple methods found matching ${name}")
                    }
                }
            }
        } else {
            def method = findMethod(self, name, args)
            if (method == null) {
                method = findBuildableMethod(self, engine, methods, args)
                if (method == null && !methods.isEmpty()) {
                    return {
                        throw new IllegalArgumentException("no compatible method ${name} found")
                    }
                }
            }
            return method
        }
    }

    /**
     * Find an adder method compatible with a specific property name and arguments.
     * @param self The command.
     * @param name The property name.
     * @param args The arguments.
     * @return A no-argument closure that either invokes the method if it is found
     * or throws an exception if there is no matching method.
     */
    static def findAdder(Object self, EvalScriptEngine engine, String name, Object... args) {
        name = "add" + name.capitalize()
        def method = findMethod(self, name, args)
        if (method == null) method = findMultiMethod(self, name, args)
        if (method == null) {
            def methods = getMethods(self, name)
            method = findBuildableMethod(self, engine, methods, args)
            if (method == null && !methods.isEmpty()) {
                return {
                    throw new IllegalArgumentException("no compatible method ${name} found")
                }
            }
        }
        return method
    }
}
