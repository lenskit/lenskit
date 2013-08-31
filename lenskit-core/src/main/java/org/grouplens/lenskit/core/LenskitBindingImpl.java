/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
}
