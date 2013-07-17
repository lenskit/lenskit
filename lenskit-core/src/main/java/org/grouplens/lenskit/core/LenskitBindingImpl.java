package org.grouplens.lenskit.core;

import org.grouplens.grapht.Binding;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

/**
 * LensKit binding implementation.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class LenskitBindingImpl<T> implements LenskitBinding<T> {
    private final Binding<T> binding;

    private LenskitBindingImpl(Binding<T> b) {
        binding = b;
    }

    static <T> LenskitBinding<T> wrap(Binding<T> binding) {
        if (binding instanceof LenskitBinding) {
            return (LenskitBinding<T>) binding;
        } else {
            return new LenskitBindingImpl<T>(binding);
        }
    }

    @Override
    public LenskitBinding<T> withQualifier(@Nonnull Class<? extends Annotation> qualifier) {
        return wrap(binding.withQualifier(qualifier));
    }

    @Override
    public LenskitBinding<T> withQualifier(@Nonnull Annotation annot) {
        return wrap(binding.withQualifier(annot));
    }

    @Override
    public LenskitBinding<T> withAnyQualifier() {
        return wrap(binding.withAnyQualifier());
    }

    @Override
    public LenskitBinding<T> unqualified() {
        return wrap(binding.unqualified());
    }

    @Override
    public LenskitBinding<T> exclude(@Nonnull Class<?> exclude) {
        return wrap(binding.exclude(exclude));
    }

    @Override
    public LenskitBinding<T> shared() {
        return wrap(binding.shared());
    }

    @Override
    public LenskitBinding<T> unshared() {
        return wrap(binding.unshared());
    }

    @Override
    public void to(@Nonnull Class<? extends T> impl, boolean chained) {
        binding.to(impl, chained);
    }

    @Override
    public void to(@Nonnull Class<? extends T> impl) {
        binding.to(impl);
    }

    @Override
    public void to(@Nullable T instance) {
        binding.to(instance);
    }

    @Override
    public void toProvider(@Nonnull Class<? extends Provider<? extends T>> provider) {
        binding.toProvider(provider);
    }

    @Override
    public void toProvider(@Nonnull Provider<? extends T> provider) {
        binding.toProvider(provider);
    }

    @Override
    public void toNull() {
        binding.toNull();
    }

    @Override
    public void toNull(Class<? extends T> type) {
        binding.toNull(type);
    }

    @Override
    public void toSymbol(TypedSymbol<? extends T> sym) {
        binding.toProvider(SymbolProvider.of(sym));
    }
}
