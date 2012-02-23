package org.grouplens.lenskit.eval.config

import java.lang.reflect.Method
import java.lang.reflect.Constructor
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.apache.commons.lang3.builder.Builder

/**
 * Search for methods on an object (typically a {@link org.apache.commons.lang3.builder.Builder})
 * for configuration.
 * @author Michael Ekstrand
 * @see BuilderDelegate
 * @since 0.10
 */
class MethodFinder {
    private static final Logger logger = LoggerFactory.getLogger(MethodFinder)
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
                if (c == null) {
                    c = maybeMakeBuilder(m, args)
                }
                if (c != null) {
                    candidates.add(c)
                }
            }
        }
        return candidates
    }

    /**
     * Set if we can directly invoke this method.
     * @param m The method to try to invoke.
     * @param args The arguments
     * @return A {@link SetterMethodCandidate} encapsulating this method with any applicable
     * transformations, or {@code null} if the method can't be used.
     */
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
        return new SetterMethodCandidate(m, args, transforms)
    }

    /**
     * See if we can use a builder to invoke this method.
     * @param m The method
     * @param args The arguments passed
     * @return A candidate that uses a builder to build the object to assign to
     * the method, or {@code null} if it can't be built with a builder.
     */
    private MethodCandidate maybeMakeBuilder(Method m, Object[] args) {
        // check args & extract closure
        if (args.length == 0) return null
        Closure closure = null
        if (args[args.length-1] instanceof Closure) {
            closure = args[args.length-1] as Closure
            args = Arrays.copyOf(args, args.length-1)
        }

        // check if this method has a single parameter with a DefaultBuilder
        Class[] formals = m.parameterTypes
        if (formals.length != 1) return null
        Class tgt = formals[0]
        DefaultBuilder bld = tgt.getAnnotation(DefaultBuilder)
        if (bld == null) return null

        Class<? extends Builder> builderCls = bld.value()
        assert builderCls != null

        // scan the builder's constructors
        Constructor<? extends Builder> ctor = null
        CTOR: for (c in builderCls.constructors) {
            formals = c.parameterTypes
            // we're looking for the right number of args, all args compatible types
            // FIXME support varargs constructors
            if (formals.length != args.length) continue

            for (i in 0..<formals.length) {
                if (!formals[i].isInstance(args[i])) {
                    continue CTOR
                }
            }
            // if we got here, then all the args are good!
            if (ctor != null) {
                // whoops, we have too many constructors. we're confused.
                logger.warning("too many constructors for {}, using the first", builderCls)
            }
            ctor = c
        }
        if (ctor == null) return null

        // OK, we can build.
        Builder builder = ctor.newInstance(args)
        return new BuilderCandidate(m, builder, closure)
    }
}
