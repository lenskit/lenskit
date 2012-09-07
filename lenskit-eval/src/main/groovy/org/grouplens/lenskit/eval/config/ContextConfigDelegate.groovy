package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.core.LenskitConfigContext

/**
 * @author Michael Ekstrand
 */
class ContextConfigDelegate {
    LenskitConfigContext context

    ContextConfigDelegate(LenskitConfigContext ctx) {
        context = ctx;
    }

    def within(Object... args) {
        if (args.length == 0) {
            throw new NoSuchMethodException("ContextConfigDelegate.within()")
        }
        Object[] reals = args
        Closure block = null
        if (args[args.length - 1] instanceof Closure) {
            block = args[args.length - 1]
            reals = Arrays.copyOf(args, args.length - 1)
        }
        LenskitConfigContext ctx = context.metaClass.invokeMethod(context, "in", reals)
        if (block != null) {
            use (ConfigHelpers) {
                block.callWithDelegate(new ContextConfigDelegate(ctx))
            }
        }
        ctx
    }

    def methodMissing(String name, args) {
        if (name in ["bind", "in", "within", "set"]) {
            context.metaClass.invokeMethod(context, name, args)
        } else {
            null
        }
    }
}
