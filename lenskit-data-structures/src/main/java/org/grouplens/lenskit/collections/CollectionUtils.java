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

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSet;

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
            final Class<?> cls = iter.getClass();
            try {
                final Method fastMethod = cls.getMethod("fastIterator");
                return new Iterable<E>() {
                    @Override
                    public Iterator<E> iterator() {
                        try {
                            return (Iterator<E>) fastMethod.invoke(iter);
                        } catch (IllegalAccessException e) {
                            return iter.iterator();
                        } catch (InvocationTargetException e) {
                            return iter.iterator();
                        }
                    }
                };
            } catch (NoSuchMethodException e) {
                return iter;
            }
        }
    }

    /**
     * Get a Fastutil {@link LongCollection} from a {@link Collection} of longs.
     * This method simply casts the collection, if possible, and returns a
     * wrapper otherwise.
     *
     * @param longs A collection of longs.
     * @return The collection as a {@link LongCollection}.
     */
    public static LongCollection fastCollection(final Collection<Long> longs) {
        if (longs instanceof LongCollection) {
            return (LongCollection) longs;
        } else {
            return new LongCollectionWrapper(longs);
        }
    }

    /**
     * Get a Fastutil {@link LongSet} from a {@link Set} of longs.
     *
     * @param longs The set of longs.
     * @return {@code longs} as a fastutil {@link LongSet}. If {@code longs} is already
     *         a LongSet, it is cast.
     */
    public static LongSet fastSet(final Set<Long> longs) {
        if (longs == null) {
            return null;
        } else if (longs instanceof LongSet) {
            return (LongSet) longs;
        } else {
            return new LongSetWrapper(longs);
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
     * Wrapper class that implements a {@link LongCollection} by delegating to
     * a {@link Collection}.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    private static class LongCollectionWrapper implements LongCollection {
        protected final Collection<Long> base;

        LongCollectionWrapper(Collection<Long> b) {
            base = b;
        }

        @Override
        public int size() {
            return base.size();
        }

        @Override
        public boolean contains(long key) {
            return base.contains(key);
        }

        @Override
        public LongIterator iterator() {
            return LongIterators.asLongIterator(base.iterator());
        }

        @Override
        public boolean add(Long item) {
            return base.add(item);
        }

        @Override
        public boolean addAll(Collection<? extends Long> items) {
            return base.addAll(items);
        }

        @Override
        public void clear() {
            base.clear();
        }

        @Override
        public boolean contains(Object item) {
            return base.contains(item);
        }

        @Override
        public boolean containsAll(Collection<?> items) {
            return base.containsAll(items);
        }

        @Override
        public boolean isEmpty() {
            return base.isEmpty();
        }

        @Override
        public boolean remove(Object item) {
            return base.remove(item);
        }

        @Override
        public boolean removeAll(Collection<?> items) {
            return base.removeAll(items);
        }

        @Override
        public boolean retainAll(Collection<?> items) {
            return base.retainAll(items);
        }

        @Override
        public Object[] toArray() {
            return base.toArray();
        }

        /**
         * {@inheritDoc}
         *
         * @deprecated see {@link LongCollection#longIterator()}
         */
        @Override
        @Deprecated
        public LongIterator longIterator() {
            return iterator();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return base.toArray(a);
        }

        @Override
        public long[] toLongArray() {
            final long[] items = new long[size()];
            LongIterators.unwrap(iterator(), items);
            return items;
        }

        @Override
        public long[] toLongArray(long[] a) {
            long[] output = a;
            if (output.length < size()) {
                output = new long[size()];
            }
            final int sz = LongIterators.unwrap(iterator(), output);
            if (sz < output.length) {
                output = Arrays.copyOf(output, sz);
            }
            return output;
        }

        @Override
        public long[] toArray(long[] a) {
            return toLongArray(a);
        }

        @Override
        public boolean add(long key) {
            return base.add(key);
        }

        @Override
        public boolean rem(long key) {
            return base.remove(key);
        }

        @Override
        public boolean addAll(LongCollection c) {
            return base.addAll(c);
        }

        @Override
        public boolean containsAll(LongCollection c) {
            return base.containsAll(c);
        }

        @Override
        public boolean removeAll(LongCollection c) {
            return base.removeAll(c);
        }

        @Override
        public boolean retainAll(LongCollection c) {
            return base.retainAll(c);
        }
    }

    static class LongSetWrapper extends LongCollectionWrapper implements LongSet {
        LongSetWrapper(Collection<Long> base) {
            super(base);
        }

        @Override
        public boolean remove(long k) {
            return super.rem(k);
        }

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
     * Wrap an iterator in a pointer.
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
