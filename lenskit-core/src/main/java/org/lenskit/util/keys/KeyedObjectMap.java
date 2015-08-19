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
package org.lenskit.util.keys;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import org.lenskit.util.BinarySearch;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.*;

/**
 * A mapping that indexes keyed objects by their keys.  This structure works on objects that have an associated key,
 * such as recommendation results or ratings (keyed by user or item ID).  The key is extracted from an object by means
 * of a {@link KeyExtractor}.  This allows the objects to be stored directly, without separate storage for keys.  This
 * map stores objects in a list sorted by key, and looks them up with binary search.  Therefore, the key extractor
 * should generally be fast (e.g. just calling a getter), in order for this class to be performant.
 */
@Immutable
public class KeyedObjectMap<T> extends AbstractLong2ObjectSortedMap<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final KeyExtractor<? super T> extractor;
    private final ImmutableList<T> data;
    private transient KeySet keySet;
    private transient EntrySet entrySet;

    public static <T> KeyedObjectMapBuilder<T> newBuilder(KeyExtractor<? super T> ex) {
        return new KeyedObjectMapBuilder<>(ex);
    }

    /**
     * Create a new keyed object map from a collection of data.
     * @param objs The input data.
     */
    public KeyedObjectMap(Collection<? extends T> objs, KeyExtractor<? super T> ex) {
        this(objs, ex, false);
    }

    @SuppressWarnings("unchecked")
    private KeyedObjectMap(Collection<? extends T> objs, KeyExtractor<? super T> ex, boolean sorted) {
        if (sorted) {
            if (objs instanceof ImmutableList) {
                data = (ImmutableList<T>) objs;
            } else {
                data = ImmutableList.copyOf(objs);
            }
        } else {
            data = Keys.keyOrdering(ex).immutableSortedCopy((Collection<T>) objs);
        }
        extractor = ex;
    }

    @Override
    public ObjectSortedSet<Entry<T>> long2ObjectEntrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    @Override
    public LongSortedSet keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public ObjectCollection<T> values() {
        return new AbstractObjectCollection<T>() {
            @Override
            public ObjectIterator<T> iterator() {
                return ObjectIterators.asObjectIterator(data.iterator());
            }

            @Override
            public int size() {
                return data.size();
            }
        };
    }

    @Override
    public LongComparator comparator() {
        return null; // keys use natural comparator
    }

    @Override
    public KeyedObjectMap<T> subMap(long from, long to) {
        int start = findIndex(from);
        int stop = findIndex(to);
        start = BinarySearch.resultToIndex(start);
        stop = BinarySearch.resultToIndex(stop);
        return new KeyedObjectMap<>(data.subList(start, stop), extractor, true);
    }

    @Override
    public KeyedObjectMap<T> headMap(long l) {
        int stop = findIndex(l);
        stop = BinarySearch.resultToIndex(stop);
        return new KeyedObjectMap<>(data.subList(0, stop), extractor, true);
    }

    @Override
    public KeyedObjectMap<T> tailMap(long l) {
        int start = findIndex(l);
        start = BinarySearch.resultToIndex(start);
        return new KeyedObjectMap<>(data.subList(start, data.size()), extractor, true);
    }

    @Override
    public long firstLongKey() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return extractor.getKey(data.get(0));
        }
    }

    @Override
    public long lastLongKey() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return extractor.getKey(data.get(data.size() - 1));
        }
    }

    private int findIndex(long k) {
        Search search = new Search(k);
        return search.search(0, data.size());
    }

    @Override
    public T get(long k) {
        int idx = findIndex(k);
        if (idx >= 0) {
            return data.get(idx);
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    private class KeySet extends AbstractLongSortedSet {
        @Override
        public LongBidirectionalIterator iterator() {
            return new KeyIterator(data.listIterator());
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public LongBidirectionalIterator iterator(long l) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LongComparator comparator() {
            return null; // keys use natural comparator
        }

        @Override
        public LongSortedSet subSet(long l, long l1) {
            return subMap(l, l1).keySet();
        }

        @Override
        public LongSortedSet headSet(long l) {
            return headMap(l).keySet();
        }

        @Override
        public LongSortedSet tailSet(long l) {
            return tailMap(l).keySet();
        }

        @Override
        public long firstLong() {
            return firstLongKey();
        }

        @Override
        public long lastLong() {
            return lastLongKey();
        }
    }

    private class EntrySet extends AbstractObjectSortedSet<Entry<T>> {
        @Override
        public ObjectBidirectionalIterator<Entry<T>> iterator() {
            return new EntryIterator(data.listIterator());
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public ObjectBidirectionalIterator<Entry<T>> iterator(Entry<T> tEntry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectSortedSet<Entry<T>> subSet(Entry<T> e1, Entry<T> e2) {
            return subMap(e1.getLongKey(), e2.getLongKey()).long2ObjectEntrySet();
        }

        @Override
        public ObjectSortedSet<Entry<T>> headSet(Entry<T> tEntry) {
            return headMap(tEntry.getLongKey()).long2ObjectEntrySet();
        }

        @Override
        public ObjectSortedSet<Entry<T>> tailSet(Entry<T> tEntry) {
            return tailMap(tEntry.getLongKey()).long2ObjectEntrySet();
        }

        @Override
        public Comparator<? super Entry<T>> comparator() {
            return null; // FIXME return a comparator
        }

        @Override
        public Entry<T> first() {
            if (data.isEmpty()) {
                throw new IllegalArgumentException();
            } else {
                T obj = data.get(0);
                return new BasicEntry<T>(extractor.getKey(obj), obj);
            }
        }

        @Override
        public Entry<T> last() {
            if (data.isEmpty()) {
                throw new IllegalArgumentException();
            } else {
                T obj = data.get(data.size() - 1);
                return new BasicEntry<T>(extractor.getKey(obj), obj);
            }
        }


    }

    private class KeyIterator extends AbstractLongBidirectionalIterator {
        private final ListIterator<T> delegate;

        public KeyIterator(ListIterator<T> iter) {
            delegate = iter;
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public long previousLong() {
            return extractor.getKey(delegate.previous());
        }

        @Override
        public long nextLong() {
            return extractor.getKey(delegate.next());
        }
    }

    private class EntryIterator extends AbstractObjectBidirectionalIterator<Entry<T>> {
        private final ListIterator<T> iter;

        public EntryIterator(ListIterator<T> it) {
            iter = it;
        }

        @Override
        public Entry<T> previous() {
            T obj = iter.previous();
            return new BasicEntry<T>(extractor.getKey(obj), obj);
        }

        @Override
        public boolean hasPrevious() {
            return iter.hasPrevious();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Entry<T> next() {
            T obj = iter.next();
            return new BasicEntry<T>(extractor.getKey(obj), obj);
        }
    }

    private class Search extends BinarySearch {
        private final long target;

        public Search(long tgt) {
            target = tgt;
        }

        @Override
        protected int test(int pos) {
            return Longs.compare(target, extractor.getKey(data.get(pos)));
        }
    }
}
