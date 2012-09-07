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
package org.grouplens.lenskit.eval.config

import org.grouplens.grapht.Binding
import org.grouplens.grapht.Context

import java.lang.annotation.Annotation
import com.google.common.base.Preconditions
import org.grouplens.lenskit.core.Parameter

/**
 * Category to extend {@link org.grouplens.grapht.Context} with additional methods.
 * @author Michael Ekstrand
 */
class ContextExtensions {
    /**
     * Create a binding that sets a parameter.
     * @param ctx The context.
     * @param param The parameter annotation.
     * @return A binding ready to set the parameter, using the type from its
     *         {@link Parameter} annotation.
     */
    static Binding set(Context ctx, Class<? extends Annotation> param) {
        Preconditions.checkNotNull(param);
        def pdef = param.getAnnotation(Parameter)
        if (pdef == null) {
            throw new IllegalArgumentException("${param} has no Parameter annotation")
        }
        def ptype = pdef.value()
        return ctx.bind(ptype).withQualifier(param)
    }
}
