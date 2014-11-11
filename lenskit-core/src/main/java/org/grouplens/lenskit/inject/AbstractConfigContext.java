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

import com.google.common.base.Preconditions;
import org.grouplens.grapht.AbstractContext;
import org.grouplens.grapht.Binding;
import org.grouplens.grapht.Context;
import org.grouplens.grapht.reflect.Qualifiers;
import org.grouplens.lenskit.core.LenskitBinding;
import org.grouplens.lenskit.core.LenskitConfigContext;
import org.grouplens.lenskit.core.Parameter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Helper for implementing Lenskit config contexts.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
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
        return bind(annot.value()).withQualifier(param);
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
}
