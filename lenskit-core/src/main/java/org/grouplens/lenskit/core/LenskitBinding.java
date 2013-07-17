package org.grouplens.lenskit.core;

import org.grouplens.grapht.Binding;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;

/**
 * LensKit-augmented binding interface.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface LenskitBinding<T> extends Binding<T> {
    @Override
    LenskitBinding<T> withQualifier(@Nonnull Class<? extends Annotation> qualifier);
    @Override
    LenskitBinding<T> withQualifier(@Nonnull Annotation annot);
    @Override
    LenskitBinding<T> withAnyQualifier();
    @Override
    LenskitBinding<T> unqualified();
    @Override
    LenskitBinding<T> exclude(@Nonnull Class<?> exclude);
    @Override
    LenskitBinding<T> shared();
    @Override
    LenskitBinding<T> unshared();

    /**
     * Bind the component to a symbol, to be resolved later (and possibly replaced after building
     * the recommender model).
     *
     * @param sym The symbol to which the component should be bound.
     */
    void toSymbol(TypedSymbol<? extends T> sym);
}
