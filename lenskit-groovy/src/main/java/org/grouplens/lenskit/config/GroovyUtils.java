/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.config;

import groovy.lang.Closure;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Map;

/**
 * Support utilities for LensKit's use of Groovy.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class GroovyUtils {
    /**
     * Call a configuration block with a specified delegate.
     * @param block The block to invoke.
     * @param delegate The delegate.
     * @return The return value.
     */
    public static Object callWithDelegate(Closure<?> block, Object delegate) {
        int oldStrategy = block.getResolveStrategy();
        Object oldDelegate = block.getDelegate();
        try {
            block.setDelegate(delegate);
            block.setResolveStrategy(Closure.DELEGATE_FIRST);
            return block.call();
        } finally {
            block.setResolveStrategy(oldStrategy);
            block.setDelegate(oldDelegate);
        }
    }

    /**
     * Build an object using named arguments.
     * @param builder The builder to use.
     * @param args The arguments.
     * @param <T> The type of object to be built.
     * @return A new object.
     */
    public static <T> T buildObject(Builder<T> builder, Map<String, Object> args) {
        for (Map.Entry<String,Object> arg: args.entrySet()) {
            String name = arg.getKey();
            // Use Groovy to invoke, since we're called from Groovy
            InvokerHelper.invokeMethod(builder, "set" + StringUtils.capitalize(name),
                                       arg.getValue());
        }
        return builder.build();
    }
}
