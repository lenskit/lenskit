package org.grouplens.lenskit.eval.config

import java.lang.reflect.Method

/**
 * A single candidate method for configuration.
 * @author Michael Ekstrand
 * @since 0.10
 * @see MethodFinder
 */
class MethodCandidate implements SettingCandidate {
    Method method;
    Closure[] transforms
    Object[] arguments

    public MethodCandidate(Method m, Object[] args) {
        method = m
        transforms = null
        arguments = args
    }

    public MethodCandidate(Method m, Object[] args, Closure[] tf) {
        method = m
        arguments = args
        transforms = tf
    }

    void invoke(Object tgt) {
        if (transforms != null) {
            def n = Math.min(transforms.length, arguments.length)
            for (int i = 0; i < n; i++) {
                if (transforms[i] != null) {
                    arguments[i] = transforms[i](arguments[i])
                }
            }
        }
        method.invoke(tgt, arguments)
    }
}
