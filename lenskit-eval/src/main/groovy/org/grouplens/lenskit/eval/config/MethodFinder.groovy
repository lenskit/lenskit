package org.grouplens.lenskit.eval.config

import groovy.transform.PackageScope
import java.lang.reflect.Method

/**
 * Search for methods on an object (typically a {@link org.apache.commons.lang3.builder.Builder})
 * for configuration.
 * @author Michael Ekstrand
 * @see BuilderDelegate
 * @since 0.10
 */
@PackageScope
class MethodFinder {
    Class clazz

    MethodFinder(Class cls) {
        clazz = cls
    }

    /**
     * Search for methods based on an invocation.
     * @param name The builder element name.
     * @param types The types of the parameters passed.
     */
    List<MethodCandidate> find(String name, Object[] args) {
        def candidates = new LinkedList<MethodCandidate>()
        def setter = "set" + name.capitalize()
        def adder = "add" + name.capitalize()
        for (m in clazz.methods) {
            if (m.name == setter || m.name == adder) {
                def c = maybeMakeCandidate(m, args)
                if (c != null) {
                    candidates.add(c)
                }
            }
        }
        return candidates
    }

    private MethodCandidate maybeMakeCandidate(Method m, Object[] args) {
        Class[] formals = m.parameterTypes
        if (formals.length != args.length) return null

        Closure[] transforms = null
        for (i in 0..<formals.length) {
            if (formals[i].isInstance(args[i])) {
                continue;
            } else if (formals[i].isAssignableFrom(File.class) && args[i] instanceof String) {
                if (transforms == null) {
                    transforms = new Closure[args.length]
                }
                transforms[i] = {s -> new File(s)}
            } else if (args[i] instanceof Class && formals[i].isAssignableFrom(args[i])) {
                if (transforms == null) {
                    transforms = new Closure[args.length]
                }
                transforms[i] = {c -> c.newInstance()}
            } else {
                return null;
            }
        }
        return new MethodCandidate(m, transforms)
    }
}
