package org.grouplens.lenskit.eval.config

import java.lang.reflect.Method
import org.apache.commons.lang3.builder.Builder
import org.apache.commons.lang3.reflect.ConstructorUtils
import org.slf4j.LoggerFactory
import static ParameterTransforms.pickInvokable

/**
 * Utilities for searching for methods of {@link Builder}s.
 * @author Michael Ekstrand
 */
class BuilderExtensions {
    private static final def logger = LoggerFactory.getLogger(BuilderExtensions)

    static def findMethod(Builder self, EvalConfigEngine engine, String name, Object[] args) {
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

        // check whether any methods exist with this name
        List<Method> methods = self.class.methods.findAll {it.name == name}
        if (methods.isEmpty()) return null

        // try to pick a method based on basic transformations
        def inv = pickInvokable(args) {self.metaClass.pickMethod(name, it)}
        if (inv != null) {
            return {
                Object[] txargs = inv.right.collect({it.get()})
                inv.left.invoke(self, txargs)
            }
        }

        // search for a single-argument method with a buildable parameter
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
            throw new RuntimeException("too many buildable options for method ${name}")
        }

        return {
            throw new IllegalArgumentException("illegal arguments for ${self.class.name}.${name}")
        }
    }

    static def findSetter(Builder self, EvalConfigEngine engine, String name, Object... args) {
        name = "set" + name.capitalize()
        return findMethod(self, engine, name, args)
    }

    static def findAdder(Builder self, EvalConfigEngine engine, String name, Object... args) {
        name = "add" + name.capitalize()
        return findMethod(self, engine, name, args)
    }
}
