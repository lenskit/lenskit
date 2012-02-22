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
    private Builder<T> builder
    private MethodFinder finder

    /**
     * Construct a new builder delegate.
     * @param builder The builder to use when pretending methods.
     */
    BuilderDelegate(Builder<T> builder) {
        this.builder = builder
        finder = new MethodFinder(builder.class)
    }

    def methodMissing(String name, args) {
        def adder = "add" + name.capitalize()
        def transforms = new Closure[args.length]
        
        def candidates = finder.find(name, args)
        if (candidates.isEmpty()) {
            throw new MissingMethodException(name, this.class, args)
        } else if (candidates.size() > 1) {
            throw new RuntimeException("too many candidate methods")
        } else {
            return candidates.get(0).invoke(builder)
        }
    }
}
