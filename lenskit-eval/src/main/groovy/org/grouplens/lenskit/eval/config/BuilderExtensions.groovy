package org.grouplens.lenskit.eval.config

import com.google.common.base.Preconditions
import java.lang.reflect.Method
import java.util.concurrent.Callable
import org.apache.commons.lang3.builder.Builder
import org.slf4j.LoggerFactory
import org.codehaus.plexus.util.StringUtils

/**
 * @author Michael Ekstrand
 */
class BuilderExtensions {
    private static final def logger = LoggerFactory.getLogger(BuilderExtensions)

    /**
     * Transform arguments, returning an array of callables yielding the converted
     * arguments. Callables are used to defer argument conversion until it is needed.
     * @param actuals The actual parameters to a method call.
     * @param formals The types of the method's formal parameters.
     * @return An array of argument thunks
     */
    static Callable[] transformArgs(Object[] actuals, Class[] formals) {
        Preconditions.checkArgument(actuals.length == formals.length)
        def tforms = new Callable[actuals.length]
        for (i in 0..<formals.length) {
            final def type = formals[i]
            final def arg = actuals[i]
            if (type.isInstance(arg)) {
                tforms[i] = {arg}
            } else if (type.isAssignableFrom(File) && arg instanceof String) {
                tforms[i] = {new File(arg as String)}
            } else if (type.isAssignableFrom(File) && arg instanceof GString) {
                tforms[i] = {new File(arg.toString())}
            } else if (arg instanceof Class && type.isAssignableFrom(arg)) {
                tforms[i] = {(arg as Class).newInstance()}
            } else {
                return null
            }
        }
        tforms
    }

    static def withTransform(Object[] args, Class[] formals, Closure cont) {
        if (formals.length == args.length) {
            def xform = transformArgs(args, formals)
            if (xform != null) {
                return {
                    Object[] xargs = xform*.call()
                    cont(xargs)
                }
            }
        }
        return null
    }

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

        // scan the methods for string or file conversions
        List<Method> methods = self.class.methods.findAll {it.name == name}
        if (methods.isEmpty()) return null

        // FIXME detect & fail on multiple valid methods
        Closure invoker = methods.collect({ m ->
            def formals = m.parameterTypes
            if (m.varArgs) {
                null
            } else {
                withTransform(args, formals) {
                    m.invoke(self, it)
                }
            }
        }).find()

        if (invoker == null) {
            // search for a single-argument method with a buildable parameter
            // FIXME this is messy and unreadable
            invoker = methods.collect({ m ->
                def formals = m.parameterTypes
                if (formals.length == 1) {
                    def type = formals[0]
                    logger.debug("looking for builder of type {}", type)
                    def bld = engine.getBuilderForType(type)
                    if (bld != null) {
                        logger.debug("using builder {}", bld)
                        Object[] cargs = args
                        Closure block = null
                        if (cargs.length > 0 && cargs[cargs.length-1] instanceof Closure) {
                            block = cargs[cargs.length-1] as Closure
                            cargs = Arrays.copyOf(cargs, cargs.length-1)
                        }
                        def invoke = bld.constructors.collect({ ctor ->
                            ctor.varArgs ? null : (withTransform(cargs, ctor.parameterTypes) {
                                ctor.newInstance(it)
                            })
                        }).find()
                        if (invoke != null) {
                            return {
                                def builder = invoke()
                                def obj = ConfigHelpers.invokeBuilder(engine, builder, block)
                                m.invoke(self, obj)
                            }
                        }
                    }
                }
                return null
            }).find()
        }

        if (invoker != null) return invoker

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
