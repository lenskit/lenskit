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
package org.grouplens.lenskit.eval.config

import org.grouplens.grapht.Module
import org.grouplens.lenskit.core.LenskitConfigContext

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ContextConfigDelegate {
    LenskitConfigContext context

    ContextConfigDelegate(LenskitConfigContext ctx) {
        context = ctx;
    }

    /**
     * Use a closure as additional configuration
     * @param cl A closure that is run on this context to do additional configuration.
     */
    def include(Closure cl) {
        use(ConfigHelpers) {
            cl.callWithDelegate(this)
        }
    }

    /**
     * Include a module in this configuration.
     * @param mod The module to include.
     */
    def include(Module mod) {
        mod.configure(context)
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
        LenskitConfigContext ctx = context.metaClass.invokeMethod(context, "within", reals)
        if (block != null) {
            use(ConfigHelpers) {
                block.callWithDelegate(new ContextConfigDelegate(ctx))
            }
        }
        ctx
    }

    def at(Object... args) {
        if (args.length == 0) {
            throw new NoSuchMethodException("ContextConfigDelegate.within()")
        }
        Object[] reals = args
        Closure block = null
        if (args[args.length - 1] instanceof Closure) {
            block = args[args.length - 1]
            reals = Arrays.copyOf(args, args.length - 1)
        }
        LenskitConfigContext ctx = context.metaClass.invokeMethod(context, "at", reals)
        if (block != null) {
            use(ConfigHelpers) {
                block.callWithDelegate(new ContextConfigDelegate(ctx))
            }
        }
        ctx
    }

    def methodMissing(String name, args) {
        if (name in ["bind", "in", "set"]) {
            context.metaClass.invokeMethod(context, name, args)
        } else {
            null
        }
    }
}
