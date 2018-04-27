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
import com.google.common.base.Preconditions;
import org.grouplens.grapht.AbstractContext;
import org.grouplens.grapht.Binding;
import org.grouplens.grapht.Context;
import org.grouplens.grapht.reflect.Qualifiers;
import org.lenskit.LenskitBinding;
import org.lenskit.LenskitConfigContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.annotation.Annotation;

/**
 * Helper for implementing Lenskit config contexts.
 *
 * @since 1.0
 */
public abstract class AbstractConfigContext extends AbstractContext implements LenskitConfigContext {
    /**
     * Coerce a Grapht context to a LensKit context.
     *
     * @param ctx The context.
     * @return A LensKit context, as a wrapper if necessary.
     */
    protected static LenskitConfigContext wrapContext(Context ctx) {
        if (ctx instanceof LenskitConfigContext) {
            return (LenskitConfigContext) ctx;
        } else {
            return new ContextWrapper(ctx);
        }
    }

    @Override
    public <T> LenskitBinding<T> bind(Class<? extends Annotation> qual, Class<T> type) {
        return LenskitBindingImpl.wrap(super.bind(qual, type));
    }

    @Override
    public <T> LenskitBinding<T> bindAny(Class<T> type) {
        return LenskitBindingImpl.wrap(super.bindAny(type));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Binding set(@Nonnull Class<? extends Annotation> param) {
        Preconditions.checkNotNull(param);
        // Parameter annotation appears on the alias target
        Class<? extends Annotation> real = Qualifiers.resolveAliases(param);
        final Parameter annot = real.getAnnotation(Parameter.class);
        if (annot == null) {
            throw new IllegalArgumentException(param.toString() + "has no Parameter annotation");
        }
        Class<?> type = annot.value();
        Binding<?> binding;
        if (type.equals(File.class)) {
            binding = LenskitBindingImpl.wrap(bind(File.class),
                                              new StringToFileConversion());
        } else {
            binding = bind(annot.value());
        }
        return binding.withQualifier(param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addComponent(@Nonnull Object obj) {
        bind((Class) obj.getClass()).toInstance(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addComponent(@Nonnull Class<?> type) {
        bind((Class) type).to(type);
    }

    @Override @Deprecated
    public LenskitConfigContext in(Class<?> type) {
        return within(type);
    }
    
    @Override @Deprecated
    public LenskitConfigContext in(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return within(qualifier, type);
    }
    
    @Override @Deprecated
    public LenskitConfigContext in(@Nullable Annotation qualifier, Class<?> type) {
        return within(qualifier, type);
    }

    private static class StringToFileConversion implements Function<Object,Optional<File>> {
        @Nullable
        @Override
        public Optional<File> apply(@Nullable Object input) {
            if (input == null) {
                return null;
            } else if (input instanceof String) {
                return Optional.of(new File((String) input));
            } else {
                return Optional.absent();
            }
        }
    }
}
