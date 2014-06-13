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
/**
 *
 */
package org.grouplens.lenskit.collections;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.grouplens.lenskit.cursors.Cursor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static it.unimi.dsi.fastutil.longs.Long2DoubleMap.FastEntrySet;

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
     * Use the fast iterator of an iterable, if available.
     *
     * @param <E>  The type of object to iterate.
     * @param iter An iterable to wrap
     * @return An iterable using the underlying iterable's fast iterator, if present,
     *         to do iteration. Fast iteration is detected by looking for a {@code fastIterator()}
     *         method, like is present in {@link FastEntrySet}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E> Iterable<E> fast(final Iterable<E> iter) {
        if (iter instanceof FastIterable) {
            return new Iterable<E>() {
                @Override
                public Iterator<E> iterator() {
                    return ((FastIterable) iter).fastIterator();
                }
            };
        } else if (iter instanceof Cursor) {
            return ((Cursor<E>) iter).fast();
        } else {
            Optional<Method> fastMethod = fastIteratorMethods.getUnchecked(iter.getClass());
            if (fastMethod.isPresent()) {
                final Method method = fastMethod.get();
                return new Iterable<E>() {
                    @Override
                    public Iterator<E> iterator() {
                        try {
                            return (Iterator<E>) method.invoke(iter);
                        } catch (IllegalAccessException e) {
                            return iter.iterator();
                        } catch (InvocationTargetException e) {
                            throw Throwables.propagate(e.getCause());
                        }
                    }
                };
            } else {
                return iter;
            }
        }
    }

    /**
     * Fast-aware filter-and-limit operation, filtering an iterable.  If the underlying iterable
     * does not support fast iteration, then the returned iterable's fast iteration will fall back
     * to ordinary iteration.
     *
     * @param iter The iterable to filter and limit.
     * @param pred The predicate for filtering.
     * @param limit The maximum number of items to return (negative for unlimited).
     * @param <E> The type of data in the iterable.
     * @return A fast iterable filtering and limiting.
     */
    public static <E> FastIterable<E> fastFilterAndLimit(Iterable<E> iter, Predicate<? super E> pred, int limit) {
        return new FilteringFastIterable<E>(iter, pred, limit);
    }

    /**
     * Cache of fast iterator methods for various classes.
     */
    private static final LoadingCache<Class<?>,Optional<Method>> fastIteratorMethods =
            CacheBuilder.newBuilder()
                        .build(new CacheLoader<Class<?>,Optional<Method>>() {
                            @Override
                            public Optional<Method> load(Class<?> key) {
                                return Optional.fromNullable(MethodUtils.getAccessibleMethod(key, "fastIterator"));
                            }
                        });

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
     * @return A list containing {@var obj} {@var n} times.
     */
    public static <T> List<T> repeat(T obj, int n) {
        return new RepeatedList<T>(obj, n);
    }

    /**
     * Create an empty, immutable fast collection.
     *
     * @param <E> The type of fast collection.
     * @return An empty fast collection.
     */
    public static <E> FastCollection<E> emptyFastCollection() {
        return new EmptyFastCollection<E>();
    }

    private static class EmptyFastCollection<E> extends AbstractCollection<E> implements FastCollection<E> {
        @Override
        public Iterator<E> fastIterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public Iterator<E> iterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public int size() {
            return 0;
        }
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
