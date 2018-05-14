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
package org.lenskit.util.keys;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import net.jcip.annotations.Immutable;
import org.lenskit.util.BinarySearch;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A map that allows objects with long keys to be looked up by key.
 * This structure works on objects that have an associated key, such as recommendation results or ratings (keyed by user
 * or item ID).  The key is extracted from an object by means of a {@link KeyExtractor}.  This allows the objects to be
 * stored directly, without separate storage for keys.  This map stores objects in a list sorted by key, and looks them
 * up with binary search.  Therefore, the key extractor should generally be fast (e.g. just calling a getter), in order
 * for this class to be performant.
 */
@Immutable
public class KeyedObjectMap<T> extends AbstractLong2ObjectSortedMap<T> implements Serializable, Iterable<T> {
    private static final long serialVersionUID = 1L;

    private final KeyExtractor<? super T> extractor;
    private final ImmutableList<T> data;
    private transient KeySet keySet;
    private transient EntrySet entrySet;

    /**
     * Create a new builder for a keyed object map.
     * @param ex The key extractor.
     * @param <T> The keyed object type.
     * @return A builder for a keyed object map.
     */
    public static <T> KeyedObjectMapBuilder<T> newBuilder(KeyExtractor<? super T> ex) {
        return new KeyedObjectMapBuilder<>(ex);
    }

    /**
     * Create a new builder for a keyed object map over a self-keying type.
     * @param <T> The keyed object type.
     * @return A builder for a keyed object map.
     */
    public static <T extends KeyedObject> KeyedObjectMapBuilder<T> newBuilder() {
        return new KeyedObjectMapBuilder<>(Keys.selfExtractor());
    }

    /**
     * Create a new keyed object map.
     * @param objs A collection of objects to put in the map.
     * @param <T> The keyed object type.
     * @return A keyed object map of the objects in {@code objs}.
     */
    public static <T extends KeyedObject> KeyedObjectMap<T> create(Collection<? extends T> objs) {
        return new KeyedObjectMap<>(objs, Keys.selfExtractor());
    }

    /**
     * Create a new keyed object map.
     * @param objs A collection of objects to put in the map.
     * @param ex The key extractor.
     * @param <T> The keyed object type.
     * @return A keyed object map of the objects in {@code objs}.
     */
    public static <T> KeyedObjectMap<T> create(Iterable<? extends T> objs, KeyExtractor<? super T> ex) {
        return new KeyedObjectMap<>(objs, ex);
    }

    /**
     * Create a new keyed object map from a collection of data.
     * @param objs The input data.
     */
    public KeyedObjectMap(Iterable<? extends T> objs, KeyExtractor<? super T> ex) {
        this(objs, ex, false);
    }

    @SuppressWarnings("unchecked")
    private KeyedObjectMap(Iterable<? extends T> objs, KeyExtractor<? super T> ex, boolean sorted) {
        if (sorted) {
            if (objs instanceof ImmutableList) {
                data = (ImmutableList<T>) objs;
            } else {
                data = ImmutableList.copyOf(objs);
            }
        } else {
            data = Keys.keyOrdering(ex).immutableSortedCopy((Iterable<T>) objs);
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
                return new ValueIterator(data.listIterator());
            }

            @Override
            public int size() {
                return data.size();
            }
        };
    }

    @Override
    public ObjectBidirectionalIterator<T> iterator() {
        return new ValueIterator(data.listIterator());
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
    public boolean containsKey(long k) {
        return findIndex(k) >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsValue(Object v) {
        if (v == null) {
            return false;
        }

        long key;
        try {
            key = extractor.getKey((T) v);
        } catch (ClassCastException ex) {
            return false;
        }
        int idx = findIndex(key);
        return idx >= 0 && data.get(idx).equals(v);
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
        public boolean contains(long k) {
            return containsKey(k);
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
                return new BasicEntry<>(extractor.getKey(obj), obj);
            }
        }

        @Override
        public Entry<T> last() {
            if (data.isEmpty()) {
                throw new IllegalArgumentException();
            } else {
                T obj = data.get(data.size() - 1);
                return new BasicEntry<>(extractor.getKey(obj), obj);
            }
        }


    }

    private class KeyIterator implements LongBidirectionalIterator {
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

    private class EntryIterator implements ObjectBidirectionalIterator<Entry<T>> {
        private final ListIterator<T> iter;

        public EntryIterator(ListIterator<T> it) {
            iter = it;
        }

        @Override
        public Entry<T> previous() {
            T obj = iter.previous();
            return new BasicEntry<>(extractor.getKey(obj), obj);
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
            return new BasicEntry<>(extractor.getKey(obj), obj);
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

    private class ValueIterator implements ObjectBidirectionalIterator<T> {
        private final ListIterator<T> delegate;

        public ValueIterator(ListIterator<T> iter) {
            delegate = iter;
        }

        @Override
        public T previous() {
            return delegate.previous();
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
        public T next() {
            return delegate.next();
        }
    }
}
