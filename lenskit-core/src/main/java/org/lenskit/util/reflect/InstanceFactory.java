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
package org.lenskit.util.reflect;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Factory function for new instances.
 *
 * @param <T> The instantiated type.
 */
public abstract class InstanceFactory<T> {
    /**
     * Create a new instance.
     * @return The new instance.
     */
    public abstract T newInstance();

    /**
     * Instance factory calling a constructor with arguments.
     * @param cls The class to instantiate.
     * @param args The arguments.
     * @param <T> The instantiated type.
     * @return An instance factory to produce new instances of {@code cls}.
     */
    public static <T> InstanceFactory<T> fromConstructor(Class<? extends T> cls, Object... args) {
        Class[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].getClass();
        }
        Constructor<? extends T> ctor = ConstructorUtils.getMatchingAccessibleConstructor(cls, types);
        if (ctor != null) {
            return new CtorInstanceFactory<T>(ctor, args);
        } else {
            throw new IllegalArgumentException("no matching constructor for " + cls);
        }
    }

    private static class CtorInstanceFactory<T> extends InstanceFactory<T> {
        private final Constructor<? extends T> constructor;
        private final Object[] arguments;

        CtorInstanceFactory(Constructor<? extends T> ctor, Object... args) {
            constructor = ctor;
            arguments = args;
        }

        @Override
        public T newInstance() {
            try {
                return constructor.newInstance(arguments);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("could not instantiate entity builder", e);
            }
        }
    }
}
