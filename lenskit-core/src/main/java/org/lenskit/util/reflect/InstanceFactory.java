/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
