package org.grouplens.lenskit.eval.config

import org.apache.commons.lang3.builder.Builder

/**
 * Basic builder delegate, configures an evaluation builder. It pretends methods
 * based on the methods provided by a particular {@link Builder} (detected via
 * reflection)
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
class BuilderDelegate<T> extends ConfigBlockDelegate {
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
