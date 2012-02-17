package org.grouplens.lenskit.eval.config

/**
 * Base delegate for processing configuration blocks.
 * @author Michael Ekstrand
 * @since 0.10
 */
abstract class ConfigBlockDelegate {
    /**
     * Process a configuration block (closure) with this delegate. The closure's
     * delegate is set to {@code this}, its resolution to {@link Closure#DELEGATE_FIRST},
     * and then {@link Closure#call()} is invoked.
     * @param cl The closure to invoke.
     * @return The result of running the closure.
     */
    def apply(Closure cl) {
        cl.setDelegate(this);
        cl.setResolveStrategy(Closure.DELEGATE_FIRST);
        return cl.call();
    }
}
