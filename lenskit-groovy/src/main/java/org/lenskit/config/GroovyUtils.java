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
package org.lenskit.config;

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
    private GroovyUtils() {
    }

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
