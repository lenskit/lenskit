package org.grouplens.lenskit.core;

import org.grouplens.grapht.Binding;
import org.grouplens.grapht.Context;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Wrap a Grapht context to make a LensKit context.
 *
 * @see 1.0
 * @author Michael Ekstrand
 */
public class ContextWrapper extends AbstractConfigContext {
    private Context base;

    public ContextWrapper(Context ctx) {
        base = ctx;
    }

    /**
     * Coerce a Grapht context to a LensKit context.
     * @param ctx The context.
     * @return A LensKit context, as a wrapper if necessary.
     */
    static LenskitConfigContext coerce(Context ctx) {
        if (ctx instanceof LenskitConfigContext) {
            return (LenskitConfigContext) ctx;
        } else {
            return new ContextWrapper(ctx);
        }
    }

    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return base.bind(type);
    }

    @Override
    public LenskitConfigContext in(Class<?> type) {
        return coerce(base.in(type));
    }

    @Override
    public LenskitConfigContext in(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return coerce(base.in(qualifier, type));
    }

    @Override
    public LenskitConfigContext in(@Nullable Annotation qualifier, Class<?> type) {
        return coerce(base.in(qualifier, type));
    }
}
