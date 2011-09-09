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
package org.grouplens.lenskit.data;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.vector.MutableSparseVector;
import org.grouplens.lenskit.vector.SparseVector;

/**
 * Array-backed implementation of {@link ScoredLongList}.  Items and scores
 * are stored in parallel arrays.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ScoredLongArrayList implements ScoredLongList, Serializable {
    private static final long serialVersionUID = 5831057078223040093L;
    private LongList items;
    private DoubleList scores;

    /**
     * Create a new list with capacity of 10.
     */
    public ScoredLongArrayList() {
        this(10);
    }

    /**
     * Create a new list with a specified initial capacity.
     */
    public ScoredLongArrayList(int capacity) {
        items = new LongArrayList(capacity);
        // scores aren't allocated until they are actually used
        scores = null;
    }

    /**
     * Create a new scored list with items from the given array and no scores.
     * @param items An array of items to copy into the list.
     */
    public ScoredLongArrayList(long[] items) {
        this.items = new LongArrayList(items);
    }

    /**
     * Create a new scored list with items and scores from the given arrays.
     * @param items An array of items to copy into the list.
     * @param scores An array of scores corresponding to the items.
     */
    public ScoredLongArrayList(long[] items, double[] scores) {
        if (scores.length != items.length)
            throw new IllegalArgumentException("array length mismatch");
        this.items = new LongArrayList(items);
        this.scores = new DoubleArrayList(scores);
    }

    protected ScoredLongArrayList(LongList items, DoubleList scores) {
        this.items = items;
        this.scores = scores;
    }

    /**
     * Compare two lists for equality. {@link Double#NaN} is considered equal to
     * itself for the purpose of this comparison.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ScoredLongList) {
            ScoredLongList oll = (ScoredLongList) o;
            if (size() != oll.size())
                return false;
            final int sz = size();
            for (int i = 0; i < sz; i++) {
                if (getLong(i) != oll.getLong(i))
                    return false;
                double s = getScore(i);
                double os = oll.getScore(i);
                if (Double.isNaN(s) ^ Double.isNaN(os))
                    return false;
                else if (!Double.isNaN(s) && !Double.isNaN(os) && s != os)
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getName());
        b.append("<[");
        final int sz = size();
        for (int i = 0; i < sz; i++) {
            if (i > 0) b.append(", ");
            b.append(getLong(i));
            b.append(": ");
            b.append(getScore(i));
        }
        b.append("]>");
        return b.toString();
    }

    @Override @Deprecated
    public LongListIterator longListIterator() {
        return iterator();
    }

    @Override @Deprecated
    public LongListIterator longListIterator(int index) {
        return listIterator(index);
    }

    @Override
    public ScoredLongListIterator listIterator(int index) {
        LongListIterator lit = items.listIterator(index);
        DoubleListIterator sit = null;
        if (scores != null)
            sit = scores.listIterator(index);
        return new Iter(lit, sit);
    }

    @Override @Deprecated
    public LongList longSubList(int from, int to) {
        return subList(from, to);
    }

    @Override
    public LongList subList(int from, int to) {
        LongList is = items.subList(from, to);
        DoubleList ss = null;
        if (scores != null)
            ss = scores.subList(from, to);
        return new ScoredLongArrayList(is, ss);
    }

    @Override
    public void size(int size) {
        items.size(size);
        if (scores != null) {
            final int ssz = scores.size();
            scores.size(size);
            if (ssz < size) {
                // grew it, install new NaNs.
                for (int i = ssz; i < size; i++)
                    scores.set(i, Double.NaN);
            }
        }
    }

    @Override
    public void getElements(int from, long[] a, int offset, int length) {
        items.getElements(from, a, offset, length);
    }

    private double[] makeNaNArray(int sz) {
        double[] a = new double[sz];
        DoubleArrays.fill(a, Double.NaN);
        return a;
    }

    @Override
    public void removeElements(int from, int to) {
        items.removeElements(from, to);
        if (scores != null)
            scores.removeElements(from, to);
    }

    @Override
    public void addElements(int index, long[] a) {
        addElements(index, a, makeNaNArray(a.length));
    }

    @Override
    public void addElements(int index, long[] a, int offset, int length) {
        addElements(index, a, makeNaNArray(a.length), offset, length);
    }

    @Override
    public boolean add(long key) {
        return add(key, Double.NaN);
    }

    @Override
    public void add(int index, long key) {
        add(index, key, Double.NaN);
    }

    @Override
    public boolean addAll(int index, LongCollection c) {
        items.addAll(index, c);
        if (scores != null) {
            // FIXME Don't allocate a new array
            scores.addElements(index, makeNaNArray(c.size()));
            assert items.size() == scores.size();
        }
        return true;
    }

    @Override
    public boolean addAll(int index, LongList c) {
        return addAll(index, (LongCollection) c);
    }

    @Override
    public boolean addAll(LongList c) {
        return addAll(0, c);
    }

    @Override
    public long getLong(int index) {
        return items.getLong(index);
    }

    @Override
    public int indexOf(long k) {
        return items.indexOf(k);
    }

    @Override
    public int lastIndexOf(long k) {
        return items.lastIndexOf(k);
    }

    @Override
    public long removeLong(int index) {
        if (scores != null)
            scores.removeDouble(index);
        return items.removeLong(index);
    }

    @Override
    public long set(int index, long k) {
        return items.set(index, k);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return items.contains(o);
    }

    @Override
    public Object[] toArray() {
        return items.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return items.toArray(a);
    }

    @Override
    public boolean add(Long e) {
        return add(e.longValue());
    }

    @Override
    public boolean remove(Object o) {
        if (scores == null) {
            return items.remove(o);
        } else {
            int i = items.indexOf(o);
            if (i >= 0) {
                items.remove(i);
                scores.remove(i);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return items.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Long> c) {
        return addAll(new LongArrayList(c));
    }

    @Override
    public boolean addAll(int index, Collection<? extends Long> c) {
        return addAll(index, new LongArrayList(c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean removed = false;
        for (Object o: c) {
            removed |= remove(o);
        }
        return removed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        items.clear();
        scores = null;
    }

    @Override
    public Long get(int index) {
        return items.get(index);
    }

    @Override
    public Long set(int index, Long element) {
        return items.set(index, element);
    }

    @Override
    public void add(int index, Long element) {
        add(index, element, Double.NaN);
    }

    @Override
    public Long remove(int index) {
        if (scores != null)
            scores.remove(index);
        return items.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return items.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return items.lastIndexOf(o);
    }

    @Override
    public int compareTo(List<? extends Long> o) {
        // FIXME Evaluate a real comparison function.
        return items.compareTo(o);
    }

    @Override @Deprecated
    public LongIterator longIterator() {
        return iterator();
    }

    @Override
    public boolean contains(long key) {
        return items.contains(key);
    }

    @Override
    public long[] toLongArray() {
        return items.toLongArray();
    }

    @Override
    public long[] toLongArray(long[] a) {
        return items.toLongArray(a);
    }

    @Override
    public long[] toArray(long[] a) {
        return items.toArray(a);
    }

    @Override
    public boolean rem(long key) {
        if (scores == null) {
            return items.rem(key);
        } else {
            int i = items.indexOf(key);
            if (i >= 0) {
                items.remove(i);
                scores.remove(i);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean addAll(LongCollection c) {
        return addAll(0, c);
    }

    @Override
    public boolean containsAll(LongCollection c) {
        return items.containsAll(c);
    }

    @Override
    public boolean removeAll(LongCollection c) {
        LongIterator it = c.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            removed |= rem(it.nextLong());
        }
        return removed;
    }

    @Override
    public boolean retainAll(LongCollection c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, long item, double score) {
        if (scores == null && !Double.isNaN(score)) {
            makeScoreList();
        }
        items.add(index, item);
        if (scores != null)
            scores.add(index, score);
    }

    private void makeScoreList() {
        final int sz = size();
        scores = new DoubleArrayList(sz);
        for (int i = 0; i < sz; i++) {
            scores.add(Double.NaN);
        }
    }

    @Override
    public boolean add(long item, double score) {
        add(size(), item, score);
        return true;
    }

    @Override
    public void addElements(int index, long[] items, double[] scores) {
        addElements(index, items, scores, 0, items.length);
    }

    @Override
    public void addElements(int index, long[] items, double[] scores,
                            int offset, int length) {
        if (this.scores == null)
            makeScoreList(); // FIXME don't need to always do this
        if (scores.length < offset + length)
            throw new ArrayIndexOutOfBoundsException(offset + length);
        this.items.addElements(index, items, offset, length);
        if (this.scores != null)
            this.scores.addElements(index, scores, offset, length);
    }

    @Override
    public void getElements(int from, long[] items, double[] scores,
                            int offset, int length) {
        this.items.getElements(from, items, offset, length);
        this.scores.getElements(from, scores, offset, length);
    }

    @Override
    public double getScore(int index) {
        if (scores == null)
            return Double.NaN;
        else
            return scores.getDouble(index);
    }

    @Override
    public double setScore(int index, double score) {
        if (scores == null && !Double.isNaN(score))
            makeScoreList();
        if (scores != null)
            return scores.set(index, score);
        else
            return Double.NaN;
    }

    @Override
    public ScoredLongListIterator iterator() {
        return listIterator();
    }

    @Override
    public ScoredLongListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public SparseVector scoreVector() {
        // FIXME Extra array copies to do this
        MutableSparseVector v = new MutableSparseVector(new LongSortedArraySet(items), Double.NaN);
        if (scores != null) {
            final int sz = size();
            for (int i = 0; i < sz; i++) {
                v.set(items.getLong(i), scores.getDouble(i));
            }
        }
        return v;
    }

    class Iter implements ScoredLongListIterator {
        final LongListIterator bitems;
        final DoubleListIterator bscores;
        double score = Double.NaN;

        public Iter(LongListIterator bi, DoubleListIterator bs) {
            bitems = bi;
            bscores = bs;
        }

        @Override
        public void set(long k) {
            bitems.set(k);
        }

        @Override
        public void add(long k) {
            bitems.add(k);
            if (bscores != null)
                bscores.add(Double.NaN);
        }

        @Override
        public boolean hasNext() {
            return bitems.hasNext();
        }

        @Override
        public Long next() {
            return nextLong();
        }

        @Override
        public boolean hasPrevious() {
            return bitems.hasPrevious();
        }

        @Override
        public Long previous() {
            return previousLong();
        }

        @Override
        public int nextIndex() {
            return bitems.nextIndex();
        }

        @Override
        public int previousIndex() {
            return bitems.previousIndex();
        }

        @Override
        public void remove() {
            bitems.remove();
            if (bscores != null)
                bscores.remove();
        }

        @Override
        public void set(Long e) {
            bitems.set(e);
        }

        @Override
        public void add(Long e) {
            add(e.longValue());
        }

        @Override
        public long previousLong() {
            if (bscores != null)
                score = bscores.previousDouble();
            return bitems.previousLong();
        }

        @Override
        public int back(int n) {
            int iv = bitems.back(n);
            int sv = bscores.back(n);
            assert sv == iv;
            return iv;
        }

        @Override
        public long nextLong() {
            if (bscores != null)
                score = bscores.nextDouble();
            return bitems.nextLong();
        }

        @Override
        public int skip(int n) {
            int iv = bitems.skip(n);
            int sv = bscores.skip(n);
            assert sv == iv;
            return iv;
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public void setScore(double s) {
            if (bscores != null)
                bscores.set(s);
            else
                throw new IllegalStateException();
        }

    }
}