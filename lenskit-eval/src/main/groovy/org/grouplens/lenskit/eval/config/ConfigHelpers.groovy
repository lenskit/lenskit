package org.grouplens.lenskit.eval.config

import org.apache.commons.lang3.builder.Builder

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
            Class[] classes = [EvalConfigEngine, builder.class]
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
        BuilderFactory factory = engine.getBuilderFactory(name)
        if (factory == null) return null

        if (args.length > 2) throw new IllegalArgumentException("${name}: too many arguments")
        Closure block = null
        String arg = null
        if (args.length > 0) {
            if (args[0] instanceof Closure) {
                if (args.length > 1) throw new IllegalArgumentException("${name}: too many arguments")
                block = args[0]
            } else {
                arg = args[0]
                if (args.length == 2) {
                    if (args[1] instanceof Closure) {
                        block = args[1]
                    } else {
                        throw new IllegalArgumentException("${name}: 2nd argument not a closure")
                    }
                }
            }
        }

        // finally have validated the arguments, move on
        return {
            def bld = factory.newBuilder(arg)
            invokeBuilder(engine, bld, block)
            bld.build()
        }
    }
}
