package org.lenskit.util.keys;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.AbstractObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.concurrent.Immutable;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An immutable long-to-double map backed by a sorted key array.
 */
@Immutable
public class Long2DoubleSortedArrayMap extends AbstractLong2DoubleSortedMap {
    private static final long serialVersionUID = 1L;

    private final LongKeyIndex index;
    private final double[] values;

    Long2DoubleSortedArrayMap(LongKeyIndex keys, double[] vs) {
        Preconditions.checkArgument(vs.length >= keys.getUpperBound(),
                                    "index and value sizes mismatched");
        index = keys;
        values = vs;
    }

    public Long2DoubleSortedArrayMap(Map<Long,Double> data) {
        Long2DoubleMap wrapped = LongUtils.asLong2DoubleMap(data);
        index = LongKeyIndex.fromCollection(wrapped.keySet());
        int size = index.size();
        values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = wrapped.get(index.getKey(i));
        }
    }

    /**
     * Create a new long-to-double map backed by a key index and a corresponding value array.
     * @param keys The keys.
     * @param vs The values (the array is used as-is, it is *not* copied).
     * @return The array map.
     */
    public static Long2DoubleSortedArrayMap wrap(LongKeyIndex keys, double[] vs) {
        return new Long2DoubleSortedArrayMap(keys, vs);
    }

    @Override
    public ObjectSortedSet<Entry> long2DoubleEntrySet() {
        return new EntrySet();
    }

    @Override
    public LongSortedSet keySet() {
        return index.keySet();
    }

    @Override
    public LongComparator comparator() {
        return null; // natural ordering
    }

    private Long2DoubleSortedMap createSubMap(int lb, int ub) {
        return new Long2DoubleSortedArrayMap(index.subIndex(lb, ub), values);
    }

    @Override
    public Long2DoubleSortedMap subMap(long from, long to) {
        int startIdx = index.findLowerBound(from); // include 'from'
        int endIdx = index.findLowerBound(to); // lower bound so we don't include 'to'
        return createSubMap(startIdx, endIdx);
    }

    @Override
    public Long2DoubleSortedMap headMap(long l) {
        int endIdx = index.findLowerBound(l); // lower bound so we don't include 'l'
        return createSubMap(index.getLowerBound(), endIdx);
    }

    @Override
    public Long2DoubleSortedMap tailMap(long l) {
        int startIdx = index.findLowerBound(l); // include 'l'
        return createSubMap(startIdx, index.getUpperBound());
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
        int idx = index.getIndex(l);
        if (idx >= 0) {
            return values[idx];
        } else {
            return defaultReturnValue();
        }
    }

    @Override
    public boolean containsKey(long k) {
        return index.containsKey(k);
    }

    @Override
    public int size() {
        return keySet().size();
    }

    private Entry entry(int idx) {
        return new BasicEntry(index.getKey(idx), values[idx]);
    }

    private class EntrySet extends AbstractObjectSortedSet<Entry> {
        @Override
        public ObjectBidirectionalIterator<Entry> iterator(Entry entry) {
            return null;
        }

        @Override
        public ObjectBidirectionalIterator<Entry> iterator() {
            return new EntryIter();
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
            return entry(index.getLowerBound());
        }

        @Override
        public Entry last() {
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            return entry(index.getUpperBound() - 1);
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry) o;
                long key = e instanceof Entry ? ((Entry) e).getLongKey() : (Long) e.getKey();
                int idx = index.getIndex(key);
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

    private class EntryIter extends AbstractObjectBidirectionalIterator<Entry> {
        IntBidirectionalIterator iter = IntIterators.fromTo(index.getLowerBound(), index.getUpperBound());

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
}
