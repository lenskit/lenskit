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

import com.google.common.base.Preconditions;
import org.grouplens.grapht.Binding;

import java.lang.annotation.Annotation;

/**
 * Helper for implementing Lenskit config contexts.
 *
 * @author Michael Ekstrand
 * @since 1.0
 */
public abstract class AbstractConfigContext implements LenskitConfigContext {
    @Override
    @SuppressWarnings("rawtypes")
    public Binding set(Class<? extends Annotation> param) {
        Preconditions.checkNotNull(param);
        final Parameter annot = param.getAnnotation(Parameter.class);
        if (annot == null) {
            throw new IllegalArgumentException(param.toString() + "has no Parameter annotation");
        }
        return bind(annot.value()).withQualifier(param);
    }
}
