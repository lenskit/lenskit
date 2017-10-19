/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.inject;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.grouplens.grapht.Binding;
import org.grouplens.grapht.reflect.Satisfaction;
import org.lenskit.LenskitBinding;
import org.lenskit.LenskitConfigContext;

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
    @Nonnull
    private final Binding<T> binding;
    @Nullable
    private final Function<Object,Optional<T>> coercion;

    private LenskitBindingImpl(@Nonnull Binding<T> b, @Nullable Function<Object,Optional<T>> coerce) {
        binding = b;
        coercion = coerce;
    }

    /**
     * Wrap a binding in a LensKit binding.
     * @param binding The binding to wrap.
     * @param <T> The bound type.
     * @return The LensKit binding wrapper.
     */
    static <T> LenskitBinding<T> wrap(Binding<T> binding) {
        return wrap(binding, null);
    }

    /**
     * Wrap a binding in a LensKit binding, with a coercion function to allow type conversions.
     * This will allow instances of other types to be bound, if the coercion function provides
     * a transformation.  Ordinarily, the generic types prevent this feature from being used, but
     * a raw type (returned from {@link LenskitConfigContext#set(Class)}
     * or commonly arising in Groovy) will allow arbitrary objects, and the coercion function will
     * convert them.  It should return an absent optional if the type
     * is unconvertable, and an optional wrapping the converted value if it is present.  If the
     * coercion fails, the original value will be used as-is (which will usually result in an error
     * from the underlying binding).
     *
     * @param binding The binding to wrap.
     * @param coercion The coercion function.
     * @param <T> The bound type.
     * @return The
     */
    static <T> LenskitBinding<T> wrap(Binding<T> binding, Function<Object,Optional<T>> coercion) {
        if (coercion == null && binding instanceof LenskitBinding) {
            return (LenskitBinding<T>) binding;
        } else {
            return new LenskitBindingImpl<>(binding, coercion);
        }
    }

    @Override
    public LenskitBinding<T> withQualifier(@Nonnull Class<? extends Annotation> qualifier) {
        return wrap(binding.withQualifier(qualifier), coercion);
    }

    @Override
    public LenskitBinding<T> withQualifier(@Nonnull Annotation annot) {
        return wrap(binding.withQualifier(annot), coercion);
    }

    @Override
    public LenskitBinding<T> withAnyQualifier() {
        return wrap(binding.withAnyQualifier(), coercion);
    }

    @Override
    public LenskitBinding<T> unqualified() {
        return wrap(binding.unqualified(), coercion);
    }

    @Override
    public LenskitBinding<T> exclude(@Nonnull Class<?> exclude) {
        return wrap(binding.exclude(exclude), coercion);
    }

    @Override
    public LenskitBinding<T> shared() {
        return wrap(binding.shared(), coercion);
    }

    @Override
    public LenskitBinding<T> unshared() {
        return wrap(binding.unshared(), coercion);
    }

    @Override
    public Binding<T> fixed() {
        return wrap(binding.fixed(), coercion);
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
        T obj = instance;
        if (coercion != null && obj != null) {
            Optional<T> result = coercion.apply(instance);
            assert result != null;
            if (result.isPresent()) {
                obj = result.get();
            }
            // otherwise, just try to use the object as-is, let Binding fail
        }
        binding.to(obj);
    }

    @Override
    public void toInstance(@Nullable T instance) {
        to(instance);
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
    public void toSatisfaction(@Nonnull Satisfaction sat) {
        binding.toSatisfaction(sat);
    }
}
