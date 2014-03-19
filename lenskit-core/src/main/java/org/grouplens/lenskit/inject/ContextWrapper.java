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
package org.grouplens.lenskit.inject;

import org.grouplens.grapht.Context;
import org.grouplens.grapht.context.ContextPattern;
import org.grouplens.lenskit.core.LenskitBinding;
import org.grouplens.lenskit.core.LenskitConfigContext;

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
