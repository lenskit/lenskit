package org.grouplens.lenskit.eval.config

import java.lang.reflect.Method
import org.apache.commons.lang3.builder.Builder
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * Method candidate that invokes a builder, then passes its result to the resulting
 * method.
 * @author Michael Ekstrand
 */
class BuilderCandidate implements MethodCandidate {
    private static final Logger logger = LoggerFactory.getLogger(BuilderCandidate)
    Method method
    Builder builder
    Closure closure
    
    BuilderCandidate(Method m, Builder b, Closure cl) {
        method = m
        builder = b
        closure = cl
    }

    @Override
    void invoke(tgt) {
        def obj = ConfigHelpers.invokeBuilder(builder, closure)
        method.invoke(tgt, obj)
    }
}
