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

import org.grouplens.grapht.Context;
import org.grouplens.grapht.context.ContextPattern;
import org.lenskit.LenskitBinding;
import org.lenskit.LenskitConfigContext;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Wrap a Grapht context to make a LensKit context.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.0
 */
class ContextWrapper extends AbstractConfigContext {
    private Context base;

    /**
     * Construct a new wrapper around a Grapht context.
     * @param ctx The Grapht context.
     */
    ContextWrapper(Context ctx) {
        base = ctx;
    }

    @Override
    public <T> LenskitBinding<T> bind(Class<T> type) {
        return LenskitBindingImpl.wrap(base.bind(type));
    }

    @Override
    public LenskitConfigContext within(Class<?> type) {
        return wrapContext(base.within(type));
    }

    @Override
    public LenskitConfigContext within(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return wrapContext(base.within(qualifier, type));
    }

    @Override
    public LenskitConfigContext within(@Nullable Annotation qualifier, Class<?> type) {
        return wrapContext(base.within(qualifier, type));
    }

    @Override
    public LenskitConfigContext matching(ContextPattern pattern) {
        return wrapContext(base.matching(pattern));
    }

    @Override
    public LenskitConfigContext at(Class<?> type) {
        return wrapContext(base.at(type));
    }

    @Override
    public LenskitConfigContext at(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return wrapContext(base.at(qualifier, type));
    }

    @Override
    public LenskitConfigContext at(@Nullable Annotation qualifier, Class<?> type) {
        return wrapContext(base.at(qualifier, type));
    }
}
