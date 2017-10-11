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
package org.lenskit;

import org.grouplens.grapht.Binding;

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
