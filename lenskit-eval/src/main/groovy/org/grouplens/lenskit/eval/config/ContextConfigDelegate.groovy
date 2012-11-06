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

import org.grouplens.lenskit.core.LenskitConfigContext

/**
 * @author Michael Ekstrand
 */
class ContextConfigDelegate {
    LenskitConfigContext context

    ContextConfigDelegate(LenskitConfigContext ctx) {
        context = ctx;
    }

    def within(Object... args) {
        if (args.length == 0) {
            throw new NoSuchMethodException("ContextConfigDelegate.within()")
        }
        Object[] reals = args
        Closure block = null
        if (args[args.length - 1] instanceof Closure) {
            block = args[args.length - 1]
            reals = Arrays.copyOf(args, args.length - 1)
        }
        LenskitConfigContext ctx = context.metaClass.invokeMethod(context, "in", reals)
        if (block != null) {
            use(ConfigHelpers) {
                block.callWithDelegate(new ContextConfigDelegate(ctx))
            }
        }
        ctx
    }

    def methodMissing(String name, args) {
        if (name in ["bind", "in", "within", "set"]) {
            context.metaClass.invokeMethod(context, name, args)
        } else {
            null
        }
    }
}
