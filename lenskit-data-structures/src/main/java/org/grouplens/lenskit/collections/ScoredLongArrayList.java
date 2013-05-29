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
package org.grouplens.lenskit.collections;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.doubles.*;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.longs.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Array-backed implementation of {@link ScoredLongList}.  Items and scores
 * are stored in parallel arrays.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public class ScoredLongArrayList implements ScoredLongList, Serializable {
    private static final long serialVersionUID = 5831057078223040093L;

    // list of items
    private LongList itemList;
    // corresponding scores - not allocated until actually used
    private DoubleList scoreList;

    /**
     * Create a new list with capacity of 10.
     */
    public ScoredLongArrayList() {
        itemList = new LongArrayList();
        scoreList = null;
    }

    /**
     * Create a new list with a specified initial capacity.
     *
     * @param capacity the initial capacity of the list.
     */
    public ScoredLongArrayList(int capacity) {
        itemList = new LongArrayList(capacity);
        scoreList = null;
    }

    /**
     * Create a new scored list with items from the given array and no scores.
     *
     * @param items An array of items to copy into the list.
     */
    public ScoredLongArrayList(long[] items) {
        itemList = new LongArrayList(items);
        scoreList = null;
    }

    /**
     * Create a new scored list with itemList and scoreList from the given arrays.
     *
     * @param items  An array of itemList to copy into the list.
     * @param scores An array of scoreList corresponding to the itemList.
     */
    public ScoredLongArrayList(long[] items, double[] scores) {
        if (scores.length != items.length) {
            throw new IllegalArgumentException("array length mismatch");
        }
        this.itemList = new LongArrayList(items);
        this.scoreList = new DoubleArrayList(scores);
    }

    /**
     * Construct a new scored list backed by pre-existing lists.
     *
     * @param items  The item list.
     * @param scores The score list. If not {@code null}, must have the same size
     *               as {@var items}.
     */
    protected ScoredLongArrayList(@Nonnull LongList items,
                                  @Nullable DoubleList scores) {
        Preconditions.checkArgument(scores == null || scores.size() == items.size(),
                                    "list size mismatch");
        itemList = items;
        scoreList = scores;
    }

    /**
     * Construct a scored list from a list of scored IDs.
     * @param items The list of scored IDs.
     */
    public ScoredLongArrayList(@Nonnull List<ScoredId> items) {
        itemList = new LongArrayList(items.size());
        scoreList = new DoubleArrayList(items.size());

        for (ScoredId id : items) {
            itemList.add(id.getId());
            scoreList.add(id.getScore());
        }
    }

    /**
     * Compare two lists for equality. {@link Double#NaN} is considered equal to
     * itself for the purpose of this comparison.
     *
     * @param o The object to compare with.
     * @return {@code true} iff this object is equal to {@var o}.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ScoredLongList) {
            ScoredLongList oll = (ScoredLongList) o;
            if (size() != oll.size()) {
                return false;
            }
            final int sz = size();
            for (int i = 0; i < sz; i++) {
                if (getLong(i) != oll.getLong(i)) {
                    return false;
                }
                double s = getScore(i);
                double os = oll.getScore(i);
                if (Double.isNaN(s) ^ Double.isNaN(os)) {
                    return false;
                } else if (!Double.isNaN(s) && !Double.isNaN(os) && s != os) {
                    return false;
                }
            }

            return true;

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        return hcb.append(itemList)
                  .append(scoreList)
                  .hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getName());
        b.append("<[");
        final int sz = size();
        for (int i = 0; i < sz; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(getLong(i));
            b.append(": ");
            b.append(getScore(i));
        }
        b.append("]>");
        return b.toString();
    }

    @Override
    public ScoredLongListIterator listIterator(int index) {
        LongListIterator lit = itemList.listIterator(index);
        DoubleListIterator sit = null;
        if (scoreList != null) {
            sit = scoreList.listIterator(index);
        }
        return new Iter(lit, sit);
    }

    @Override
    public LongList subList(int from, int to) {
        LongList is = itemList.subList(from, to);
        DoubleList ss = null;
        if (scoreList != null) {
            ss = scoreList.subList(from, to);
        }
        return new ScoredLongArrayList(is, ss);
    }

    @Override
    public void size(int size) {
        itemList.size(size);
        if (scoreList != null) {
            final int ssz = scoreList.size();
            scoreList.size(size);
            if (ssz < size) {
                // grew it, install new NaNs.
                for (int i = ssz; i < size; i++) {
                    scoreList.set(i, Double.NaN);
                }
            }
        }
    }

    @Override
    public void getElements(int from, long[] a, int offset, int length) {
        itemList.getElements(from, a, offset, length);
    }

    private double[] makeNaNArray(int sz) {
        double[] a = new double[sz];
        DoubleArrays.fill(a, Double.NaN);
        return a;
    }

    @Override
    public void removeElements(int from, int to) {
        itemList.removeElements(from, to);
        if (scoreList != null) {
            scoreList.removeElements(from, to);
        }
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
        itemList.addAll(index, c);
        if (scoreList != null) {
            scoreList.addAll(index, CollectionUtils.repeat(Double.NaN, c.size()));
            assert itemList.size() == scoreList.size();
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
        return itemList.getLong(index);
    }

    @Override
    public int indexOf(long k) {
        return itemList.indexOf(k);
    }

    @Override
    public int lastIndexOf(long k) {
        return itemList.lastIndexOf(k);
    }

    @Override
    public long removeLong(int index) {
        if (scoreList != null) {
            scoreList.removeDouble(index);
        }
        return itemList.removeLong(index);
    }

    @Override
    public long set(int index, long k) {
        return itemList.set(index, k);
    }

    @Override
    public int size() {
        return itemList.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return itemList.contains(o);
    }

    @Override
    public Object[] toArray() {
        return itemList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return itemList.toArray(a);
    }

    @Override
    public boolean add(Long e) {
        return add(e.longValue());
    }

    @Override
    public boolean remove(Object o) {
        if (scoreList == null) {
            return itemList.remove(o);
        } else {
            int i = itemList.indexOf(o);
            if (i >= 0) {
                itemList.remove(i);
                scoreList.remove(i);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return itemList.containsAll(c);
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
        for (Object o : c) {
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
        itemList.clear();
        scoreList = null;
    }

    @Override
    public Long get(int index) {
        return itemList.get(index);
    }

    @Override
    public Long set(int index, Long element) {
        return itemList.set(index, element);
    }

    @Override
    public void add(int index, Long element) {
        add(index, element, Double.NaN);
    }

    @Override
    public Long remove(int index) {
        if (scoreList != null) {
            scoreList.remove(index);
        }
        return itemList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return itemList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return itemList.lastIndexOf(o);
    }

    @Override
    public int compareTo(List<? extends Long> o) {
        return itemList.compareTo(o);
    }

    @Override
    public boolean contains(long key) {
        return itemList.contains(key);
    }

    @Override
    public long[] toLongArray() {
        return itemList.toLongArray();
    }

    @Override
    public long[] toLongArray(long[] a) {
        return itemList.toLongArray(a);
    }

    @Override
    public long[] toArray(long[] a) {
        return itemList.toArray(a);
    }

    @Override
    public boolean rem(long key) {
        if (scoreList == null) {
            return itemList.rem(key);
        } else {
            int i = itemList.indexOf(key);
            if (i >= 0) {
                itemList.remove(i);
                scoreList.remove(i);
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
        return itemList.containsAll(c);
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
        if (!Double.isNaN(score)) {
            ensureScoreList();
        }
        itemList.add(index, item);
        if (scoreList != null) {
            scoreList.add(index, score);
        }
    }

    private void ensureScoreList() {
        if (scoreList == null) {
            final int sz = size();
            scoreList = new DoubleArrayList(CollectionUtils.repeat(Double.NaN, sz));
        }
    }

    @Override
    public boolean add(long item, double score) {
        add(size(), item, score);
        return true;
    }

    @Override
    public void addElements(int index, @Nonnull long[] items, @Nonnull double[] scores) {
        addElements(index, items, scores, 0, items.length);
    }

    @Override
    public void addElements(int index, @Nonnull long[] items, @Nonnull double[] scores,
                            int offset, int length) {
        ensureScoreList();

        if (items.length < offset + length) {
            throw new ArrayIndexOutOfBoundsException(offset + length);
        }
        if (scores.length < offset + length) {
            throw new ArrayIndexOutOfBoundsException(offset + length);
        }

        this.itemList.addElements(index, items, offset, length);

        if (this.scoreList != null) {
            this.scoreList.addElements(index, scores, offset, length);
        }
    }

    @Override
    public void getElements(int from, long[] items, double[] scores,
                            int offset, int length) {
        itemList.getElements(from, items, offset, length);
        if (scoreList != null) {
            scoreList.getElements(from, scores, offset, length);
        } else {
            DoubleArrays.fill(scores, offset, length, Double.NaN);
        }
    }

    @Override
    public double getScore(int index) {
        if (scoreList == null) {
            return Double.NaN;
        } else {
            return scoreList.getDouble(index);
        }
    }

    @Override
    public double setScore(int index, double score) {
        if (!Double.isNaN(score)) {
            ensureScoreList();
        }
        if (scoreList != null) {
            return scoreList.set(index, score);
        } else {
            return Double.NaN;
        }
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
        MutableSparseVector v = new MutableSparseVector(new LongSortedArraySet(itemList), Double.NaN);
        if (scoreList != null) {
            final int sz = size();
            for (int i = 0; i < sz; i++) {
                v.set(itemList.getLong(i), scoreList.getDouble(i));
            }
        }
        return v;
    }

    /**
     * Sort the entries of this list by score.
     *
     * @param comp The comparator for odering by score.
     */
    public void sort(final DoubleComparator comp) {
        if (scoreList == null) {
            return; // no scoreList, no order
        }

        IntComparator idxc = new AbstractIntComparator() {
            @Override
            public int compare(int k1, int k2) {
                return comp.compare(scoreList.get(k1), scoreList.get(k2));
            }
        };

        Arrays.quickSort(0, size(), idxc, new Swap());
    }

    /**
     * If the underlying lists are array lists, trim them to capacity.
     */
    public void trim() {
        if (itemList instanceof LongArrayList) {
            ((LongArrayList) itemList).trim();
        }
        if (scoreList instanceof DoubleArrayList) {
            ((DoubleArrayList) scoreList).trim();
        }
    }

    //CHECKSTYLE:OFF MissingDeprecated
    @Override
    @Deprecated
    public LongListIterator longListIterator() {
        return iterator();
    }

    @Override
    @Deprecated
    public LongListIterator longListIterator(int index) {
        return listIterator(index);
    }

    @Override
    @Deprecated
    public LongList longSubList(int from, int to) {
        return subList(from, to);
    }

    @Override
    @Deprecated
    public LongIterator longIterator() {
        return iterator();
    }
    //CHECKSTYLE:ON

    private class Swap implements Swapper {
        @Override
        public void swap(int a, int b) {
            long old = itemList.set(a, itemList.getLong(b));
            itemList.set(b, old);
            double olds = scoreList.set(a, scoreList.getDouble(b));
            scoreList.set(b, olds);
        }
    }

    private static class Iter implements ScoredLongListIterator {
        private final LongListIterator bitems;
        private final DoubleListIterator bscores;
        private double score = Double.NaN;
        private boolean active = false;

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
            if (bscores != null) {
                bscores.add(Double.NaN);
            }
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
            if (bscores != null) {
                bscores.remove();
            }
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
            if (bscores != null) {
                score = bscores.previousDouble();
            }
            active = true;
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
            if (bscores != null) {
                score = bscores.nextDouble();
            }
            active = true;
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
            if (!active) {
                throw new IllegalStateException("attempted to get score without next or previous");
            }
            return score;
        }

        @Override
        public void setScore(double s) {
            if (bscores != null) {
                bscores.set(s);
            } else {
                throw new IllegalStateException();
            }
        }

    }
}
