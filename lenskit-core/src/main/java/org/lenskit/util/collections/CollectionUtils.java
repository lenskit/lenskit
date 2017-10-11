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
/**
 *
 */
package org.lenskit.util.collections;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

import java.util.Collection;
import java.util.List;

/**
 * Various helper methods for working with collections (particularly Fastutil
 * collections).
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    /**
     * Wrap a {@link Collection} in an {@link ObjectCollection}.
     *
     * @param objects The collection of objects.
     * @param <E>     The type of objects.
     * @return The collection as an {@link ObjectCollection}.
     */
    public static <E> ObjectCollection<E> objectCollection(Collection<E> objects) {
        if (objects instanceof ObjectCollection) {
            return (ObjectCollection<E>) objects;
        } else {
            return new ObjectCollectionWrapper<>(objects);
        }
    }

    /**
     * Return a list that repeats a single object multiple times.
     *
     * @param obj The object.
     * @param n   The size of the list.
     * @param <T> The type of list elements.
     * @return A list containing <var>obj</var> <var>n</var> times.
     */
    public static <T> List<T> repeat(T obj, int n) {
        return new RepeatedList<>(obj, n);
    }

    /**
     * Create an {@link IntList} that contains all numbers in a specified interval.
     *
     * @param from The first number (inclusive)
     * @param to   the last number (exclusive).
     * @return A list containing the integers in the interval {@code [from,to)}.
     */
    public static IntList interval(int from, int to) {
        Preconditions.checkArgument(to >= from, "last integer less than first");
        return new IntIntervalList(from, to);
    }
}
