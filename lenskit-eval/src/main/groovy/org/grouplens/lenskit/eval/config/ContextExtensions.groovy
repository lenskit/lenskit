package org.grouplens.lenskit.eval.config

import org.grouplens.grapht.Binding
import org.grouplens.grapht.Context

import java.lang.annotation.Annotation

/**
 * Category to extend {@link org.grouplens.grapht.Context} with additional methods.
 * @author Michael Ekstrand
 */
class ContextExtensions {
    static <T> Binding<T> bind(Context ctx, Class<? extends Annotation> qual, Class<T> cls) {
        return ctx.bind(cls).withQualifier(qual)
    }
}
