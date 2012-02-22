package org.grouplens.lenskit.eval.config

import java.lang.reflect.Method
import org.apache.commons.lang3.builder.Builder
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * @author Michael Ekstrand
 */
class BuilderCandidate implements SettingCandidate {
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
        logger.debug("invoking {} with builder {}", method, builder)
        def obj
        if (closure != null) {
            logger.debug("invoking closure")
            def delegate = new BuilderDelegate(builder)
            obj = delegate.apply(closure)
        } else {
            obj = builder.build()
        }
        method.invoke(tgt, obj)
    }
}
