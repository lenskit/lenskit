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
package org.grouplens.lenskit.util;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.PrimitiveSink;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Functional-style utilities to go with Guava.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Functional {
    private Functional() {}

    /**
     * A function that wraps objects according to an equivalence relation.
     * @param equiv The equivalence.
     * @param <T> The type of object to wrap.
     * @return A function that wraps its arguments in an equivalence wrapper.
     * @see Equivalence#wrap(Object)
     */
    public static <T> Function<T,Equivalence.Wrapper<T>> equivWrap(Equivalence<T> equiv) {
        return new EquivWrap<>(equiv);
    }

    /**
     * A {@link Funnel} that serializes an object to the sink.
     * @return A funnel that funnels objects by serializing them.
     */
    public static Funnel<Object> serializeFunnel() {
        return SerializeFunnel.INSTANCE;
    }

    /**
     * Identity function casting its arguments to a particular type.
     *
     * @param <F> The function's input type.
     * @param <T> The type to which to cast arguments.
     * @param target The target type for arguments.
     * @return A function which, when applied to an object, casts it to type
     *         <var>target</var>.
     */
    public static <F, T> Function<F, T> cast(final Class<T> target) {
        return new Function<F, T>() {
            @Override
            public T apply(F obj) {
                return target.cast(obj);
            }
        };
    }

    private static enum SerializeFunnel implements Funnel<Object> {
        INSTANCE;

        @Override
        public void funnel(Object from, PrimitiveSink into) {
            try (ObjectOutputStream out = new ObjectOutputStream(Funnels.asOutputStream(into))) {
                out.writeObject(from);
            } catch (IOException ex) {
                throw Throwables.propagate(ex);
            }
        }
    }

    private static class EquivWrap<T> implements Function<T,Equivalence.Wrapper<T>> {
        private final Equivalence<T> equivalence;

        public EquivWrap(Equivalence<T> equiv) {
            equivalence = equiv;
        }

        @Nullable
        @Override
        public Equivalence.Wrapper<T> apply(@Nullable T input) {
            if (input == null) {
                return null;
            } else {
                return equivalence.wrap(input);
            }
        }
    }
}
