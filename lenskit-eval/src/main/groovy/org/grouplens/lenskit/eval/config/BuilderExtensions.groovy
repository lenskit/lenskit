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

import java.lang.reflect.Method
import org.apache.commons.lang3.builder.Builder
import org.apache.commons.lang3.reflect.ConstructorUtils
import org.slf4j.LoggerFactory
import static ParameterTransforms.pickInvokable
import com.google.common.base.Supplier
import org.apache.commons.lang3.reflect.TypeUtils

/**
 * Utilities for searching for methods of {@link Builder}s.
 * @author Michael Ekstrand
 */
class BuilderExtensions {
    private static final def logger = LoggerFactory.getLogger(BuilderExtensions)

    /**
     * Find a method compatible with some arguments.
     * @param self The builder.
     * @param name The method name.
     * @param args The arguments.
     * @return A closure invoking the method, or {@code null}.
     */
    static def findMethod(Builder self, String name, Object[] args) {
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
     * Search for a single-argument method with a parameter that can be built.
     * @param self The builder to search.
     * @param engine The config engine.
     * @param name The method name.
     * @param args The arguments.
     * @return A closure using a builder to build an object and then pass it to a single-parameter
     * method, or {@code null} if no such method can be found.
     * @see EvalConfigEngine#getBuilderForType(Class)
     */
    static def findBuildableMethod(Builder self, EvalConfigEngine engine, List<Method> methods, Object[] args) {
        // FIXME this is messy and unreadable
        def buildables = methods.collect({ method ->
            def formals = method.parameterTypes
            if (formals.length == 1) {
                def type = formals[0]
                logger.debug("looking for builder of type {}", type)
                Class bld = engine.getBuilderForType(type)
                if (bld != null) {
                    logger.debug("using builder {}", bld)
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
                            def obj = ConfigHelpers.invokeBuilder(engine, builder, block)
                            method.invoke(self, obj)
                        }
                    }
                }
            }
            return null
        }).findAll()

        if (buildables.size() == 1) {
            return buildables.get(0)
        } else if (buildables.size() > 1) {
            throw new RuntimeException("too many buildable options")
        } else {
            return null
        }
    }

    static def findMultiMethod(Builder self, String name, Object[] args) {
        if (args.length != 1) return null
        if (!(args[0] instanceof Supplier)) return null
        def arg = args[0] as Supplier

        // unpack the type
        def type = arg.class
        // get the type arguments for Supplier to instantiate this type
        def asn = TypeUtils.getTypeArguments(type, Supplier)
        def lstType = asn?.get(Supplier.typeParameters[0])
        if (lstType == null) return null // we can't resolve the list type
        // Does the supplier supply a list?
        if (List.class.isAssignableFrom(TypeUtils.getRawType(lstType, null))) {
            // Unpack the type of element in the list
            def lstAsn = TypeUtils.getTypeArguments(lstType, List)
            def eltType = lstAsn?.get(List.typeParameters[0])
            assert eltType != null
            Class[] atypes = [eltType]
            MetaMethod mm = self.metaClass.pickMethod(name, atypes)
            if (mm != null) {
                return {
                    for (elt in arg.get()) {
                        mm.doMethodInvoke(self, elt)
                    }
                }
            }
        }
    }

    static List<Method> getMethods(Builder self, String name) {
        self.class.methods.findAll {it.name == name}
    }

    static def findSetter(Builder self, EvalConfigEngine engine, String name, Object... args) {
        name = "set" + name.capitalize()
        def method = findMethod(self, name, args)
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

    static def findAdder(Builder self, EvalConfigEngine engine, String name, Object... args) {
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
