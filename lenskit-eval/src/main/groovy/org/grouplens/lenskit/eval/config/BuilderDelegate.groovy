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
    private MethodFinder finder

    /**
     * Construct a new builder delegate.
     * @param builder The builder to use when pretending methods.
     */
    BuilderDelegate(EvalConfigEngine engine, Builder<T> builder) {
        this.engine = engine
        this.builder = builder
        finder = new MethodFinder(engine, builder.class)
    }

    def methodMissing(String name, args) {
        def adder = "add" + name.capitalize()
        def transforms = new Closure[args.length]
        
        def candidates = finder.find(name, args)
        Closure method

        if (candidates.isEmpty()) {
            method = ConfigHelpers.findBuilderMethod(engine, name, args)
        } else if (candidates.size() > 1) {
            throw new RuntimeException("too many candidate methods")
        } else {
            method = candidates.get(0)
        }

        if (method == null) {
            throw new MissingMethodException(name, this.class, args)
        } else {
            method.setDelegate(builder)
            method.call()
        }
    }
}
