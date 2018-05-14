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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.AbstractObjectSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.jcip.annotations.Immutable;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Scalars;

import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;

import static it.unimi.dsi.fastutil.Arrays.quickSort;

/**
 * An immutable long-to-double map backed by a sorted key array.
 */
@Immutable
public final class Long2DoubleSortedArrayMap extends AbstractLong2DoubleSortedMap {
    private static final long serialVersionUID = 1L;


    private final SortedKeyIndex keys;
    private final double[] values;

    Long2DoubleSortedArrayMap(SortedKeyIndex ks, double[] vs) {
        Preconditions.checkArgument(vs.length >= ks.getUpperBound(),
                                    "index and value sizes mismatched");
        keys = ks;
        values = vs;
    }

    /**
     * Create a new map with existing data.
     * @param data Use {@link #create(Map)} instead, as it can avoid copying maps that are already packed.
     */
    @Deprecated
    public Long2DoubleSortedArrayMap(Map<Long,Double> data) {
        Long2DoubleFunction vf = LongUtils.asLong2DoubleFunction(data);
        keys = SortedKeyIndex.fromCollection(data.keySet());
        int size = keys.size();
        values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = vf.get(keys.getKey(i));
        }
    }

    /**
     * Create a new long-to-double sorted array map from another map.
     *
     * Using this method instead of the constructor allows copies of immutable vectors
     * to be skipped.
     *
     * @param input The vector to copy.
     * @return A new vector with the same data as {@code input}.
     */
    public static Long2DoubleSortedArrayMap create(Long2DoubleMap input) {
        if (input instanceof Long2DoubleSortedArrayMap) {
            return (Long2DoubleSortedArrayMap) input;
        } else {
            return new Long2DoubleSortedArrayMap(input);
        }
    }

    /**
     * Create a new long-to-double map backed by a key index and a corresponding value array.
     * @param keys The keys.
     * @param vs The values (the array is used as-is, it is *not* copied).
     * @return The array map.
     */
    public static Long2DoubleSortedArrayMap wrap(SortedKeyIndex keys, double[] vs) {
        return new Long2DoubleSortedArrayMap(keys, vs);
    }

    /**
     * Create a new {@link Long2DoubleSortedArrayMap} from unsorted key and value
     * arrays. The provided arrays will be modified and should not be used
     * by the client after this operation has completed. The key domain of
     * the new {@link Long2DoubleSortedArrayMap} will be the same as {@code keys}.
     *
     * @param keys Array of entry keys. This should be duplicate-free.
     * @param values The values of the vector, in key order.
     * @return A sparse vector backed by the provided arrays.
     * @throws IllegalArgumentException if there is a problem with the provided
     *                                  arrays (length mismatch, etc.).
     */
    public static Long2DoubleSortedArrayMap wrapUnsorted(long[] keys, double[] values) {
        IdComparator comparator = new IdComparator(keys);
        ParallelSwapper swapper = new ParallelSwapper(keys, values);
        quickSort(0, keys.length, comparator, swapper);

        int n = keys.length;
        for (int i = 1; i < n; i++) {
            if (keys[i-1] == keys[i]) {
                throw new IllegalArgumentException("duplicate keys");
            }
        }

        SortedKeyIndex index = SortedKeyIndex.wrap(keys, keys.length);

        return wrap(index, values);
    }

    /**
     * Create a map from an array and index mapping.
     *
     * @param mapping The index mapping specifying the keys.
     * @param values The array of values.
     * @return A sparse vector mapping the IDs in {@code map} to the values in {@code values}.
     * @throws IllegalArgumentException if {@code values} not the same size as {@code idx}.
     */
    public static Long2DoubleSortedArrayMap fromArray(KeyIndex mapping, double[] values) {
        return fromArray(mapping, DoubleArrayList.wrap(values));
    }

    /**
     * Create a map from an array and index mapping.
     *
     * @param mapping The index mapping specifying the keys.
     * @param values The array of values.
     * @return A sparse vector mapping the IDs in {@code map} to the values in {@code values}.
     * @throws IllegalArgumentException if {@code values} not the same size as {@code idx}.
     */
    public static Long2DoubleSortedArrayMap fromArray(KeyIndex mapping, DoubleList values) {
        Preconditions.checkArgument(values.size() == mapping.size(),
                                    "value array and index have different sizes: " + values.size() + " != " + mapping.size());
        final int n = values.size();
        double[] nvs = new double[n];
        SortedKeyIndex index = SortedKeyIndex.fromCollection(mapping.getKeyList());
        for (int i = 0; i < n; i++) {
            long item = index.getKey(i);
            int origIndex = mapping.getIndex(item);
            nvs[i] = values.getDouble(origIndex);
        }
        return wrap(index, nvs);
    }

    /**
     * Create a new sorted array map from input data.
     * @param data The input data.
     * @return The sorted array map.
     */
    @SuppressWarnings("deprecation")
    public static Long2DoubleSortedArrayMap create(Map<Long,Double> data) {
        if (data instanceof Long2DoubleSortedArrayMap) {
            return (Long2DoubleSortedArrayMap) data;
        } else {
            return new Long2DoubleSortedArrayMap(data);
        }
    }

    @Override
    public FastSortedEntrySet long2DoubleEntrySet() {
        return new EntrySet();
    }

    @Override
    public LongSortedArraySet keySet() {
        return keys.keySet();
    }

    /**
     * Get a key by its position in the map. Used for optimizing certain operations.
     * @param i The index.
     * @return The key at position {@code i}.
     */
    public long getKeyByIndex(int i) {
        return keys.getKey(i + keys.getLowerBound());
    }

    /**
     * Get a value by its position in the map. Used for optimizing certain operations.
     * @param i The index.
     * @return The value at position {@code i}.
     */
    public double getValueByIndex(int i) {
        return values[i + keys.getLowerBound()];
    }

    @Override
    public LongComparator comparator() {
        return null; // natural ordering
    }

    private Long2DoubleSortedArrayMap createSubMap(int lb, int ub) {
        return new Long2DoubleSortedArrayMap(keys.subIndex(lb, ub), values);
    }

    @Override
    public Long2DoubleSortedArrayMap subMap(long from, long to) {
        int startIdx = keys.findLowerBound(from); // include 'from'
        int endIdx = keys.findLowerBound(to); // lower bound so we don't include 'to'
        return createSubMap(startIdx, endIdx);
    }

    /**
     * Return a subset of this map containing only the keys that appear in another set.
     * @param toKeep The set of keys to keep.
     * @return A copy of this map containing only those keys that appear in {@code keys}.
     */
    public Long2DoubleSortedArrayMap subMap(LongSet toKeep) {
        if (toKeep == keySet()) {
            return this;
        }

        if (toKeep instanceof LongSortedArraySet) {
            return fastSubMap((LongSortedArraySet) toKeep);
        } else {
            return slowSubMap(toKeep);
        }
    }

    private Long2DoubleSortedArrayMap slowSubMap(LongSet toKeep) {
        LongSortedSet kept = LongUtils.setIntersect(keySet(), toKeep);
        double[] nvs = new double[kept.size()];
        int i = keys.getLowerBound();
        int j = 0;
        LongIterator iter = kept.iterator();
        while (iter.hasNext()) {
            long key = iter.nextLong();
            while (keys.getKey(i) < key) {
                i++;
            }
            nvs[j] = values[i];
            j++;
            i++;
        }
        return wrap(SortedKeyIndex.fromCollection(kept), nvs);
    }

    private Long2DoubleSortedArrayMap fastSubMap(LongSortedArraySet toKeep) {
        SortedKeyIndex oks = toKeep.getIndex();
        int tn = size();
        int on = oks.size();
        long[] nks = new long[Math.min(tn, on)];
        double[] nvs = new double[Math.min(tn, on)];
        int tlb = keys.getLowerBound();
        int olb = oks.getLowerBound();
        int ni = 0;

        if (on * Scalars.log2(tn) < tn) {
            for (int oi = 0; oi < on; oi++) {
                long k = oks.getKey(oi + olb);
                int ti = keys.tryGetIndex(k);
                if (ti >= 0) {
                    nks[ni] = k;
                    nvs[ni] = values[ti];
                    ni++;
                }
            }
        } else {
            int ti = 0, oi = 0;
            int lti = -1, loi = -1;
            long tk = 0, ok = 0;

            while (ti < tn && oi < on) {
                if (ti != lti) {
                    tk = keys.getKey(tlb + ti);
                    lti = ti;
                }
                if (oi != loi) {
                    ok = oks.getKey(olb + oi);
                    loi = oi;
                }
                if (tk == ok) {
                    nks[ni] = tk;
                    nvs[ni] = values[ti + tlb];
                    ni++;
                    ti++;
                    oi++;
                } else if (tk < ok) {
                    ti++;
                } else {
                    oi++;
                }
            }
        }

        return wrap(SortedKeyIndex.wrap(nks, ni), nvs);
    }

    @Override
    public Long2DoubleSortedMap headMap(long l) {
        int endIdx = keys.findLowerBound(l); // lower bound so we don't include 'l'
        return createSubMap(keys.getLowerBound(), endIdx);
    }

    @Override
    public Long2DoubleSortedMap tailMap(long l) {
        int startIdx = keys.findLowerBound(l); // include 'l'
        return createSubMap(startIdx, keys.getUpperBound());
    }

    @Override
    public long firstLongKey() {
        return keySet().firstLong();
    }

    @Override
    public long lastLongKey() {
        return keySet().lastLong();
    }

    @Override
    public double get(long l) {
        int idx = keys.tryGetIndex(l);
        if (idx >= 0) {
            return values[idx];
        } else {
            return defaultReturnValue();
        }
    }

    @Override
    public boolean containsKey(long k) {
        return keys.containsKey(k);
    }

    @Override
    public int size() {
        return keys.size();
    }

    private Entry entry(int idx) {
        return new BasicEntry(keys.getKey(idx), values[idx]);
    }

    private class EntrySet extends AbstractObjectSortedSet<Entry> implements FastSortedEntrySet {
        @Override
        public ObjectBidirectionalIterator<Entry> iterator(Entry entry) {
            return new EntryIter(entry.getLongKey());
        }

        @Override
        public ObjectBidirectionalIterator<Entry> iterator() {
            return new EntryIter();
        }

        @Override
        public ObjectBidirectionalIterator<Entry> fastIterator(Entry entry) {
            return new EntryIter(entry.getLongKey());
        }

        @Override
        public ObjectBidirectionalIterator<Entry> fastIterator() {
            return new FastEntryIter();
        }

        @Override
        public Comparator<? super Entry> comparator() {
            return null;
        }

        @Override
        public ObjectSortedSet<Entry> subSet(Entry from, Entry to) {
            return subMap(from.getLongKey(), to.getLongKey()).long2DoubleEntrySet();
        }

        @Override
        public ObjectSortedSet<Entry> headSet(Entry entry) {
            return headMap(entry.getLongKey()).long2DoubleEntrySet();
        }

        @Override
        public ObjectSortedSet<Entry> tailSet(Entry entry) {
            return tailMap(entry.getLongKey()).long2DoubleEntrySet();
        }

        @Override
        public Entry first() {
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            return entry(keys.getLowerBound());
        }

        @Override
        public Entry last() {
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            return entry(keys.getUpperBound() - 1);
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry) o;
                long key = e instanceof Entry ? ((Entry) e).getLongKey() : (Long) e.getKey();
                int idx = keys.tryGetIndex(key);
                if (idx >= 0) {
                    return e.getValue().equals(values[idx]);
                }
            }
            return false;
        }

        @Override
        public int size() {
            return keySet().size();
        }
    }

    private class EntryIter implements ObjectBidirectionalIterator<Entry> {
        IntBidirectionalIterator iter;

        public EntryIter() {
            iter = IntIterators.fromTo(keys.getLowerBound(), keys.getUpperBound());
        }

        public EntryIter(long k) {
            this();
            // skip past the item
            int idx = keys.findUpperBound(k);
            iter.skip(idx);
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Entry next() {
            return entry(iter.nextInt());
        }

        @Override
        public Entry previous() {
            return entry(iter.previousInt());
        }

        @Override
        public boolean hasPrevious() {
            return iter.hasPrevious();
        }
    }

    private class FastEntryIter implements ObjectBidirectionalIterator<Entry> {
        IntBidirectionalIterator iter = IntIterators.fromTo(keys.getLowerBound(), keys.getUpperBound());
        IndirectEntry entry = new IndirectEntry(0);

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Entry next() {
            entry.index = iter.nextInt();
            return entry;
        }

        @Override
        public Entry previous() {
            entry.index = iter.nextInt();
            return entry;
        }

        @Override
        public boolean hasPrevious() {
            return iter.hasPrevious();
        }
    }

    private class IndirectEntry implements Entry {
        int index;

        public IndirectEntry(int idx) {
            index = idx;
        }

        @Override
        public long getLongKey() {
            return keys.getKey(index);
        }

        @Override
        public double setValue(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getDoubleValue() {
            return values[index];
        }

        @Override
        @SuppressWarnings("deprecation")
        public Long getKey() {
            return getLongKey();
        }

        @Override
        public Double getValue() {
            return getDoubleValue();
        }

        @Override
        @SuppressWarnings("deprecation")
        public Double setValue(Double value) {
            throw new UnsupportedOperationException();
        }
    }

    private static class IdComparator implements IntComparator {
        private long[] keys;

        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        public IdComparator(long[] keys) {
            this.keys = keys;
        }

        @Override
        public int compare(int i, int i2) {
            return LongComparators.NATURAL_COMPARATOR.compare(keys[i], keys[i2]);
        }
    }

    private static class ParallelSwapper implements Swapper {

        private long[] keys;
        private double[] values;

        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        public ParallelSwapper(long[] keys, double[] values) {
            this.keys = keys;
            this.values = values;
        }

        @Override
        public void swap(int i, int i2) {
            long lTemp = keys[i];
            keys[i] = keys[i2];
            keys[i2] = lTemp;

            double dTemp = values[i];
            values[i] = values[i2];
            values[i2] = dTemp;
        }
    }
}
