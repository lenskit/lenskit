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
    private EvalConfigEngine engine
    private Builder<T> builder

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
