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
import org.grouplens.grapht.Context;
import org.grouplens.grapht.context.ContextPattern;
import org.lenskit.inject.Parameter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * LensKit-specific augmentations of the Grapht context interface.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @since 1.0
 */
public interface LenskitConfigContext extends Context {
    @Override
    <T> LenskitBinding<T> bind(Class<T> type);
    @Override
    <T> LenskitBinding<T> bind(Class<? extends Annotation> qual, Class<T> type);
    @Override
    <T> LenskitBinding<T> bindAny(Class<T> type);

    /**
     * Start a binding that sets a parameter. Parameters are qualifiers that
     * bear the {@link Parameter} annotation.
     *
     * @param param The parameter to set.
     * @return The binding for this parameter. It is untyped, as many types will
     *         be accepted and attempted.
     */
    @SuppressWarnings("rawtypes")
    Binding set(Class<? extends Annotation> param);

    /**
     * Add a component object to the injector.  This is the equivalent of:
     * <pre>{@code
     * this.bind(obj.getClass()).to(obj);
     * }</pre>
     * <p>It has the result of making {@code obj} available satisfy dependencies on its class or,
     * via supertype binding generation, any of its supertypes.  Explicit bindings for those
     * supertypes will override this binding.</p>
     *
     * @param obj The object to register.
     */
    void addComponent(@Nonnull Object obj);

    /**
     * Add a component type to the injector.  This is the equivalent of:
     * <pre>{@code
     * this.bind(type).to(type);
     * }</pre>
     * <p>It has the result of making {@code type} available satisfy dependencies on itself or,
     * via supertype binding generation, any of its supertypes.  Explicit bindings for those
     * supertypes will override this binding.</p>
     *
     * @param type The type to register.
     */
    void addComponent(@Nonnull Class<?> type);
    
    @Override
    @Deprecated
    LenskitConfigContext in(Class<?> type);
    
    @Override
    @Deprecated
    LenskitConfigContext in(@Nullable Class<? extends Annotation> qualifier,
                                Class<?> type);
    
    @Override
    @Deprecated
    LenskitConfigContext in(@Nullable Annotation qualifier, Class<?> type);

    @Override
    LenskitConfigContext within(Class<?> type);

    @Override
    LenskitConfigContext within(@Nullable Class<? extends Annotation> qualifier,
                            Class<?> type);

    @Override
    LenskitConfigContext within(@Nullable Annotation qualifier, Class<?> type);

    @Override
    LenskitConfigContext matching(ContextPattern pattern);

    @Override
    LenskitConfigContext at(Class<?> type);

    @Override
    LenskitConfigContext at(@Nullable Class<? extends Annotation> qualifier,
                                Class<?> type);

    @Override
    LenskitConfigContext at(@Nullable Annotation qualifier, Class<?> type);
}
