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
package org.grouplens.lenskit.collections;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Deprecated
public final class Pointers {
    private Pointers() {}

    /**
     * Create a pointer over a range of integers.
     * @param start The first integer (inclusive).
     * @param end The last integer (exclusive).
     * @return An int pointer over the range.
     */
    public static IntPointer fromTo(int start, int end) {
        Preconditions.checkArgument(end >= start, "end is less than start");
        return new IntervalPointer(start, end);
    }

    private static final class IntervalPointer extends AbstractIntPointer {
        private final int end;
        private int current;

        public IntervalPointer(int s, int e) {
            current = s;
            end = e;
        }

        @Override
        public boolean advance(){
            if (current < end) {
                current += 1;
            }
            return current < end;
        }

        @Override
        public int getInt() {
            if (current < end) {
                return current;
            } else {
                throw new NoSuchElementException("range pointer exhausted");
            }
        }

        @Override
        public boolean isAtEnd() {
            return current >= end;
        }
    }

    /**
     * Transform a pointer.
     * @param pointer The pointer to transform.
     * @param func A function to apply.  This function is called on each call to {@link Pointer#get()}}.
     * @param <T> The type of the underlying pointer.
     * @param <R> The type of the transformed pointer.
     * @return A transformed pointer.
     */
    public static <T,R> Pointer<R> transform(Pointer<T> pointer, Function<? super T, ? extends R> func) {
        return new TransformedPointer<T,R>(pointer, func);
    }

    private static class TransformedPointer<T,R> implements Pointer<R> {
        private final Pointer<T> pointer;
        private final Function<? super T, ? extends R> function;

        public TransformedPointer(Pointer<T> ptr, Function<? super T, ? extends R> func) {
            pointer = ptr;
            function = func;
        }

        @Override
        public boolean advance() {
            return pointer.advance();
        }

        @Override
        public R get() {
            return function.apply(pointer.get());
        }

        @Override
        public boolean isAtEnd() {
            return pointer.isAtEnd();
        }
    }

    /**
     * Wrap an iterator in a pointer.  It is safe for this iterator to be a fast iterator; the resulting pointer
     * may then return the same object, modified, multiple times.
     *
     * @param <E>  The type of value in the iterator.
     * @param iter The iterator to wrap.
     * @return A pointer backed by the iterator.
     * @see Pointer
     * @since 0.9
     */
    public static <E> Pointer<E> fromIterator(Iterator<E> iter) {
        return new IteratorPointer<E>(iter);
    }
}
