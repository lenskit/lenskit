/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
     * @param objects The collection of objects.
     * @param <E> The type of objects.
     * @return The collection as an {@link ObjectCollection}.
     */
    public static <E> ObjectCollection<E> objectCollection(Collection<E> objects) {
        if (objects instanceof ObjectCollection) {
            return (ObjectCollection<E>) objects;
        } else {
            return new ObjectCollectionWrapper<E>(objects);
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
        return new RepeatedList<T>(obj, n);
    }

    /**
     * Create an {@link IntList} that contains all numbers in a specified interval.
     * @param from The first number (inclusive)
     * @param to the last number (exclusive).
     * @return A list containing the integers in the interval {@code [from,to)}.
     */
    public static IntList interval(int from, int to) {
        Preconditions.checkArgument(to >= from, "last integer less than first");
        return new IntIntervalList(from, to);
    }
}
