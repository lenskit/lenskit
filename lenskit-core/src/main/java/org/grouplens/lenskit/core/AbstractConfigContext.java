package org.grouplens.lenskit.core;

import com.google.common.base.Preconditions;
import org.grouplens.grapht.Binding;

import java.lang.annotation.Annotation;

/**
 * Helper for implementing Lenskit config contexts.
 *
 * @since 1.0
 * @author Michael Ekstrand
 */
public abstract class AbstractConfigContext implements LenskitConfigContext {
    @Override
    @SuppressWarnings("rawtypes")
    public Binding set(Class<? extends Annotation> param) {
        Preconditions.checkNotNull(param);
        final Parameter annot = param.getAnnotation(Parameter.class);
        if (annot == null) {
            throw new IllegalArgumentException(param.toString() + "has no Parameter annotation");
        }
        return bind(annot.value()).withQualifier(param);
    }
}
