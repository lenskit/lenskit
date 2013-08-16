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
/**
 *
 */
package org.grouplens.lenskit.collections;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
        if (iter instanceof FastCollection) {
            return new Iterable<E>() {
                @Override
                public Iterator<E> iterator() {
                    return ((FastCollection) iter).fastIterator();
                }
            };
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
     * Get a Fastutil {@link it.unimi.dsi.fastutil.longs.LongCollection} from a {@link java.util.Collection} of longs.
     * This method simply casts the collection, if possible, and returns a
     * wrapper otherwise.
     *
     * @param longs A collection of longs.
     * @return The collection as a {@link it.unimi.dsi.fastutil.longs.LongCollection}.
     * @deprecated Use {@link LongUtils#asLongCollection(Collection)} instead.
     */
    @Deprecated
    public static LongCollection fastCollection(final Collection<Long> longs) {
        return LongUtils.asLongCollection(longs);
    }

    /**
     * Get a Fastutil {@link it.unimi.dsi.fastutil.longs.LongSet} from a {@link Set} of longs.
     *
     * @param longs The set of longs.
     * @return {@code longs} as a fastutil {@link it.unimi.dsi.fastutil.longs.LongSet}. If {@code longs} is already
     *         a LongSet, it is cast.
     * @deprecated Use {@link LongUtils#asLongSet(Set)} instead.
     */
    @Deprecated
    public static LongSet fastSet(final Set<Long> longs) {
        return LongUtils.asLongSet(longs);
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
    public static <E> Pointer<E> pointer(Iterator<E> iter) {
        return new IteratorPointer<E>(iter);
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
}
