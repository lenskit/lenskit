package org.grouplens.lenskit.eval.config

/**
 * @author Michael Ekstrand
 */
abstract class AbstractDelegate {
    def apply(Closure cl) {
        cl.setDelegate(this);
        cl.setResolveStrategy(Closure.DELEGATE_FIRST);
        return cl.call();
    }
}
