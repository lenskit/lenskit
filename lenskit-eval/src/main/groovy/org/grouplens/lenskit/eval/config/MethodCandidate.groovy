package org.grouplens.lenskit.eval.config

import groovy.transform.PackageScope
import java.lang.reflect.Method

/**
 * A single candidate method for configuration.
 * @author Michael Ekstrand
 * @since 0.10
 * @see MethodFinder
 */
@PackageScope
class MethodCandidate {
    Method method;
    Closure[] transforms;

    public MethodCandidate(Method m) {
        method = m
        transforms = null
    }

    public MethodCandidate(Method m, Closure... tf) {
        method = m
        transforms = tf
    }

    def invoke(Object tgt, Object... args) {
        if (transforms != null) {
            def n = Math.min(transforms.length, args.length)
            for (int i = 0; i < n; i++) {
                if (transforms[i] != null) {
                    args[i] = transforms[i](args[i])
                }
            }
        }
        method.invoke(tgt, args)
    }
}
