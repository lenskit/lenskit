package org.grouplens.lenskit.eval.config

import org.grouplens.grapht.Binding
import org.grouplens.grapht.Context

import java.lang.annotation.Annotation
import org.grouplens.grapht.annotation.Parameter
import com.google.common.base.Preconditions

/**
 * Category to extend {@link org.grouplens.grapht.Context} with additional methods.
 * @author Michael Ekstrand
 */
class ContextExtensions {
    static <T> Binding<T> bind(Context ctx, Class<? extends Annotation> qual, Class<T> cls) {
        return ctx.bind(cls).withQualifier(qual)
    }

    /**
     * Create a binding that sets a parameter.
     * @param ctx The context.
     * @param param The parameter annotation.
     * @return A binding ready to set the parameter, using the type from its
     *         {@link Parameter} annotation.
     */
    static Binding set(Context ctx, Class<? extends Annotation> param) {
        Preconditions.checkNotNull(param);
        def pdef = param.getAnnotation(Parameter)
        if (pdef == null) {
            throw new IllegalArgumentException("${param} has no Parameter annotation")
        }
        def ptype = pdef.value().getType()
        return ctx.bind(ptype).withQualifier(param)
    }
}
