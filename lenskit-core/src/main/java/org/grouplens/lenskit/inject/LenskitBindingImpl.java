/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.inject;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.grouplens.grapht.Binding;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.lenskit.core.LenskitBinding;

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
     * a raw type (returned from {@link org.grouplens.lenskit.core.LenskitConfigContext#set(Class)}
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
            return new LenskitBindingImpl<T>(binding, coercion);
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
