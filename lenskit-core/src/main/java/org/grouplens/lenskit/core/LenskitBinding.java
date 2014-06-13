/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
import java.lang.annotation.Annotation;

/**
 * LensKit-augmented binding interface.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface LenskitBinding<T> extends Binding<T> {
    /**
     * Explicitly bind to an instance.
     * @param instance The instance to bind to.
     */
    void toInstance(@Nullable T instance);

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
}
