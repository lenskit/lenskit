/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
 * @author Michael Ekstrand
 * @since 1.0
 * @compat Public
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
    LenskitConfigContext in(Class<?> type);

    @Override
    LenskitConfigContext in(@Nullable Class<? extends Annotation> qualifier,
                            Class<?> type);

    @Override
    LenskitConfigContext in(@Nullable Annotation qualifier, Class<?> type);
}
