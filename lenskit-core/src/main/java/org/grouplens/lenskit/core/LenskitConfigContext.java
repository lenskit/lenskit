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
import org.grouplens.grapht.Context;

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
}
