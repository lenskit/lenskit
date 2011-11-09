/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Iterators;

/**
 * Various helper methods for working with collections (particularly Fastutil
 * collections).
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CollectionUtils {
    /**
     * Get a Fastutil {@link LongCollection} from a {@link Collection} of longs.
     * This method simply casts the collection, if possible, and returns a
     * wrapper otherwise.
     * @param longs A collection of longs.
     * @return The collection as a {@link LongCollection}.
     */
    public static LongCollection fastCollection(final Collection<Long> longs) {
        if (longs instanceof LongCollection)
            return (LongCollection) longs;
        else
            return new LongCollectionWrapper(longs);
    }

    /**
     * Get a Fastutil {@link LongSet} from a {@link Set} of longs.
     */
    public static LongSet fastSet(final Set<Long> longs) {
        if (longs == null)
            return null;
        else if (longs instanceof LongSet)
            return (LongSet) longs;
        else
            return new LongSetWrapper(longs);
    }

    /**
     * Wrapper class that implements a {@link LongCollection} by delegating to
     * a {@link Collection}.
     * @author Michael Ekstrand <ekstrand@cs.umn.edu>
     *
     */
    static class LongCollectionWrapper implements LongCollection {
        final protected Collection<Long> base;
        LongCollectionWrapper(Collection<Long> base) {
            this.base = base;
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
        @Override @Deprecated
        public LongIterator longIterator() {
            return iterator();
        }
        @Override
        public <T> T[] toArray(T[] a) {
            return base.toArray(a);
        }
        @Override
        public long[] toLongArray() {
            long[] items = new long[size()];
            LongIterators.unwrap(iterator(), items);
            return items;
        }
        @Override
        public long[] toLongArray(long[] a) {
            if (a.length < size())
                a = new long[size()];
            LongIterators.unwrap(iterator(), a);
            return a;
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
     * Get a {@link LongIterator} which to iterate over a collection.
     * This facilitiates iteration without boxing if the underlying collection
     * is a Fastutil {@link LongCollection}.
     * @see #fastIterator(Iterator)
     * @param col The collection of longs.
     * @return A Fastutil iterator for the collection.
     */
    @Deprecated
    public static LongIterator fastIterator(final Collection<Long> col) {
        return fastIterator(col.iterator());
    }

    /**
     * Cast or wrap an iterator to a Fastutil {@link LongIterator}.
     * @param iter An iterator of longs.
     * @return A Fastutil iterator wrapping <var>iter</var>.  If <var>iter</var>
     * is already a Fastutil iterator (an instance of {@link LongIterator}), this
     * is simply <var>iter</var> cast to {@link LongIterator}.  Otherwise, it is
     * a wrapper object.
     * @deprecated Use {@link LongIterators#asLongIterator(Iterator)} instead.
     */
    @Deprecated
    public static LongIterator fastIterator(final Iterator<Long> iter) {
        return LongIterators.asLongIterator(iter);
    }

    public static <E> FastCollection<E> emptyFastCollection() {
        return new EmptyFastCollection<E>();
    }
    
    /**
     * Wrap an iterator in a pointer.
     * @param iter The iterator to wrap.
     * @return A pointer backed by the iterator.
     * @see Pointer
     * @since 0.9
     */
    public static <E> Pointer<E> pointer(Iterator<E> iter) {
        return new IteratorPointer<E>(iter);
    }

    static class EmptyFastCollection<E> extends AbstractCollection<E> implements FastCollection<E> {

        @Override
        public Iterator<E> fastIterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public Iterable<E> fast() {
            return this;
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
