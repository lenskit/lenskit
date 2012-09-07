package org.grouplens.lenskit.core;

import org.grouplens.grapht.Binding;
import org.grouplens.grapht.Context;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * LensKit-specific augmentations of the Grapht context interface.
 *
 * @author Michael Ekstrand
 * @since 1.0
 * @compat Public
 */
public interface LenskitConfigContext extends Context {
    /**
     * Start a binding that sets a parameter. Parameters are qualifiers that
     * bear the {@link Parameter} annotation.
     *
     * @param param The parameter to set.
     * @return The binding for this parameter. It is untyped, as many types will
     *         be accepted and attempted.
     */
    @SuppressWarnings("rawtypes")
    Binding set(Class<? extends Annotation> param);

    @Override
    LenskitConfigContext in(Class<?> type);

    @Override
    LenskitConfigContext in(@Nullable Class<? extends Annotation> qualifier,
                            Class<?> type);

    @Override
    LenskitConfigContext in(@Nullable Annotation qualifier, Class<?> type);
}
