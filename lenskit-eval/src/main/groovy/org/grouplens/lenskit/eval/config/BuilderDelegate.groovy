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

    /**
     * Construct a new builder delegate.
     * @param builder The builder to use when pretending methods.
     */
    BuilderDelegate(Builder<T> builder) {
        this.builder = builder
    }

    def methodMissing(String name, args) {
        def adder = "add" + name.capitalize()
        def transforms = new Closure[args.length]
        for (m in builder.class.methods) {
            Class[] atypes = m.parameterTypes
            if (m.name == adder && atypes.length == args.length) {
                boolean good = true;
                for (int i = 0; good && i < atypes.length; i++) {
                    def arg = args[i]
                    if (atypes[i].isAssignableFrom(arg.class)) {
                        transforms[i] = null
                    } else if (arg instanceof Class && atypes[i].isAssignableFrom(arg)) {
                        transforms[i] = {cls -> cls.newInstance()}
                    } else {
                        good = false;
                    }
                }
                if (good) {
                    for (int i = 0; i < args.length; i++) {
                        if (transforms[i] != null) {
                            args[i] = transforms[i](args[i]);
                        }
                    }
                    return m.invoke(builder, args);
                }
            }
        }
        // if we got this far, we found nothing
        throw new MissingMethodException(name, this.class, args);
    }
}
