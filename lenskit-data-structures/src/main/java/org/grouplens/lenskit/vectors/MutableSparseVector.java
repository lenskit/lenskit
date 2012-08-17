/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.BitSetIterator;
import org.grouplens.lenskit.collections.IntIntervalList;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.collections.MoreArrays;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * Mutable sparse vector interface
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 *         <p>This extends the sparse vector with support for imperative mutation
 *         operations on their values, but
 *         once created the set of keys remains immutable.  Addition and subtraction are
 *         supported.  Mutation operations also operate in-place to reduce the
 *         reallocation and copying required.  Therefore, a common pattern is:
 *
 *         <pre>
 *                 MutableSparseVector normalized = MutableSparseVector.copy(vector);
 *                 normalized.subtract(normFactor);
 *                 </pre>
 * @compat Public
 */
public class MutableSparseVector extends SparseVector implements Serializable {

    private static final long serialVersionUID = 1L;
    protected final long[] keys;
    protected final BitSet usedKeys;
    protected double[] values;
    protected final int domainSize;

    /**
     * Construct a new empty vector. Since it also has an empty key domain, this
     * doesn't do much.
     */
    public MutableSparseVector() {
        this(new long[0], new double[0]);
    }

    /**
     * Construct a new vector from the contents of a map. The key domain is the
     * key set of the map.
     * @param ratings A map providing the values for the vector.
     */
    public MutableSparseVector(Long2DoubleMap ratings) {
        keys = ratings.keySet().toLongArray();
        domainSize = keys.length;
        Arrays.sort(keys);
        assert keys.length == ratings.size();
        assert MoreArrays.isSorted(keys, 0, domainSize);
        values = new double[keys.length];
        final int len = keys.length;
        for (int i = 0; i < len; i++) {
            values[i] = ratings.get(keys[i]);
        }
        usedKeys = new BitSet(domainSize);
        usedKeys.set(0, domainSize);
    }

    /**
     * Construct a new empty vector with specified key domain.
     * @param domain The key domain.
     */
    public MutableSparseVector(Collection<Long> domain) {
        LongSortedArraySet set;
        // since LSAS is immutable, we'll use its array if we can!
        if (domain instanceof LongSortedArraySet) {
            set = (LongSortedArraySet) domain;
        } else {
            set = new LongSortedArraySet(domain);
        }
        keys = set.unsafeArray();
        values = new double[keys.length];
        domainSize = keys.length;
        usedKeys = new BitSet(domainSize);
    }

    /**
     * Construct a new vector with specified keys, setting all values to a constant
     * value.
     * @param keySet The keys to include in the vector.
     * @param value The value to assign for all keys.
     */
    public MutableSparseVector(LongSet keySet, double value) {
        this(keySet);
        DoubleArrays.fill(values, 0, domainSize, value);
        usedKeys.set(0, domainSize);
    }

    /**
     * Construct a new vector from existing arrays.  It is assumed that the keys
     * are sorted and duplicate-free, and that the values is the same length. The
     * key array is the key domain, and all keys are considered used.
     * @param keys The array of keys backing this vector. They must be sorted.
     * @param values The array of values backing this vector.
     */
    protected MutableSparseVector(long[] keys, double[] values) {
        this(keys, values, keys.length);
    }

    /**
     * Construct a new vector from existing arrays. It is assumed that the keys
     * are sorted and duplicate-free, and that the values is the same length.
     * The key set and key domain is the keys array, and both are the keys
     * array.
     *
     * @param keys The array of keys backing the vector. It must be sorted.
     * @param values The array of values backing the vector.
     * @param length Number of items to actually use.
     */
    protected MutableSparseVector(long[] keys, double[] values, int length) {
        this.keys = keys;
        this.values = values;
        domainSize = length;
        usedKeys = new BitSet(length);
        for (int i = 0; i < length; i++) {
            usedKeys.set(i);
        }
    }

    /**
     * Construct a new vector from existing arrays. It is assumed that the keys
     * are sorted and duplicate-free, and that the values is the same length.
     * The key set and key domain is the keys array, and both are the keys
     * array.
     *
     * @param keys The array of keys backing the vector.
     * @param values The array of values backing the vector.
     * @param length Number of items to actually use.
     * @param used The entries in use.
     */
    protected MutableSparseVector(long[] keys, double[] values, int length, BitSet used) {
        this.keys = keys;
        this.values = values;
        domainSize = length;
        usedKeys = used;
    }

    protected void checkValid() {
        if (values == null) {
            throw new IllegalStateException("Vector is frozen");
        }
    }

    protected int findIndex(long key) {
        return Arrays.binarySearch(keys, 0, domainSize, key);
    }

    @Override
    public int size() {
        return usedKeys.cardinality();
    }

    @Override
    public LongSortedSet keyDomain() {
        return LongSortedArraySet.wrap(keys, domainSize);
    }

    @Override
    public LongSortedSet keySet() {
        return LongSortedArraySet.wrap(keys, domainSize, usedKeys);
    }

    @Override
    public DoubleCollection values() {
        checkValid();
        DoubleArrayList lst = new DoubleArrayList(size());
        BitSetIterator iter = new BitSetIterator(usedKeys, 0, domainSize);
        while (iter.hasNext()) {
            int idx = iter.nextInt();
            lst.add(values[idx]);
        }
        return lst;
    }

    @Override
    public Iterator<VectorEntry> iterator() {
        return new IterImpl();
    }

    @Override
    public Iterator<VectorEntry> fastIterator(VectorEntry.State state) {
        IntIterator iter;
        switch (state) {
        case SET:
            iter = new BitSetIterator(usedKeys, 0, domainSize);
            break;
        case UNSET: {
            BitSet unused = (BitSet) usedKeys.clone();
            unused.flip(0, domainSize);
            iter = new BitSetIterator(unused, 0, domainSize);
            break;
        }
        case EITHER: {
            iter = new IntIntervalList(0, domainSize).iterator();
            break;
        }
        default:
            throw new IllegalArgumentException("invalid entry state");
        }
        return new FastIterImpl(iter);
    }

    @Override
    public final boolean containsKey(long key) {
        final int idx = findIndex(key);
        return idx >= 0 && usedKeys.get(idx);
    }

    @Override
    public final double get(long key, double dft) {
        checkValid();
        final int idx = findIndex(key);
        if (idx >= 0) {
            if (usedKeys.get(idx)) {
                return values[idx];
            } else {
                return dft;
            }
        } else {
            return dft;
        }
    }

    private double setAt(int idx, double value) {
        if (idx >= 0) {
            final double v = usedKeys.get(idx) ? values[idx] : Double.NaN;
            values[idx] = value;
            clearCachedValues();
            usedKeys.set(idx);
            return v;
        } else {
            return Double.NaN;
        }
    }

    /**
     * Set a value in the vector.
     *
     * @param key The key of the value to set.
     * @param value The value to set.
     * @return The original value, or {@link Double#NaN} if the key had no value
     *         (or if the original value was {@link Double#NaN}).
     */
    public final double set(long key, double value) {
        checkValid();
        final int idx = findIndex(key);
        return setAt(idx, value);
    }

    /**
     * Set the value in the vector corresponding to a vector entry. This is
     * used in lieu of providing a {@code setValue} method on {@link VectorEntry},
     * and changes the value in constant time. The value on the entry is also changed
     * to reflect the new value.
     *
     * @param entry The entry to update.
     * @param value The new value.
     * @return The old value.
     * @throws IllegalArgumentException if {@code entry} does not come from this vector.
     */
    public final double set(VectorEntry entry, double value) {
        if (entry.vector != this) {
            throw new IllegalArgumentException("entry not from correct vector");
        }
        final int idx = entry.getIndex();
        entry.setValue(value);
        return setAt(idx, value);
    }

    /**
     * Set the values for all items in the key domain to {@code value}.
     * @param value The value to set.
     */
    public final void fill(double value) {
        DoubleArrays.fill(values, 0, domainSize, value);
        usedKeys.set(0, domainSize);
    }

    /**
     * Clear the value for a key.  The key remains in the key domain, but is
     * removed from the key set.
     *
     * @param key The key to clear.
     */
    public final void clear(long key) {
        final int idx = findIndex(key);
        if (idx >= 0) {
            usedKeys.clear(idx);
        }
    }

    /**
     * Clear the value for a vector entry.
     * @param e The entry to clear.
     * @see #clear(long)
     */
    public final void clear(VectorEntry e) {
        if (e.vector != this) {
            throw new IllegalArgumentException("clearing vector from wrong entry");
        }
        usedKeys.clear(e.getIndex());
    }

    /**
     * Clear all values from the set.
     */
    public void clear() {
        usedKeys.clear();
    }

    /**
     * Add a value to the specified entry. The value must be in the key set.
     * @param key The key whose value should be added.
     * @param value The value to increase it by.
     * @return The new value (or {@link Double#NaN} if no such key existed).
     */
    public final double add(long key, double value) {
        checkValid();
        final int idx = findIndex(key);
        if (idx >= 0 && usedKeys.get(idx)) {
            clearCachedValues();
            return values[idx] += value;
        } else {
            return Double.NaN;
        }
    }

    /**
     * Add a value to the specified entry, setting the value if the key is not
     * in the key set.
     *
     * @param key The key whose value should be added.
     * @param value The value to increase it by.
     * @return The new value. If the key is not in the key domain,
     *         {@link Double#NaN} is returned.
     * @deprecated Generally not wanted. Will be removed by LensKit 1.0.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public final double addOrSet(long key, double value) {
        checkValid();
        final int idx = findIndex(key);
        if (idx >= 0) {
            clearCachedValues();
            if (usedKeys.get(idx)) {
                return values[idx] += value;
            } else {
                values[idx] = value;
                usedKeys.set(idx);
                return Double.NaN;
            }
        } else {
            return Double.NaN;
        }
    }

    /**
     * Subtract another rating vector from this one.
     *
     * <p>After calling this method, every element of this vector has been
     * decreased by the corresponding element in <var>other</var>.  Elements
     * with no corresponding element are unchanged.
     *
     * @param other The vector to subtract.
     */
    public final void subtract(final SparseVector other) {
        checkValid();
        clearCachedValues();
        int i = 0;
        for (VectorEntry oe : other.fast()) {
            final long k = oe.getKey();
            while (i < domainSize && keys[i] < k) {
                i++;
            }
            if (i >= domainSize) {
                break; // no more entries
            }
            if (keys[i] == k && usedKeys.get(i)) {
                values[i] -= oe.getValue();
            } // otherwise, key is greater; advance outer
        }
    }

    /**
     * Add another rating vector to this one.
     *
     * <p>After calling this method, every element of this vector has been
     * increased by the corresponding element in <var>other</var>.  Elements
     * with no corresponding element are unchanged.
     * @param other The vector to add.
     */
    public final void add(final SparseVector other) {
        checkValid();
        clearCachedValues();
        int i = 0;
        for (VectorEntry oe : other.fast()) {
            final long k = oe.getKey();
            while (i < domainSize && keys[i] < k) {
                i++;
            }
            if (i >= domainSize) {
                break; // no more entries
            }
            if (keys[i] == k && usedKeys.get(i)) {
                values[i] += oe.getValue();
            } // otherwise, key is greater; advance outer 
        }
    }

    /**
     * Set the values in this SparseVector to equal the values in
     * <var>other</var> for each key that is present in both vectors.
     *
     * <p>After calling this method, every element in this vector that has a key
     * in <var>other</var> has its value set to the corresponding value in
     * <var>other</var>. Elements with no corresponding key are unchanged, and
     * elements in <var>other</var> that are not in this vector are not
     * inserted.
     *
     * @param other The vector to blit its values into this vector
     */
    public final void set(final SparseVector other) {
        checkValid();
        clearCachedValues();
        int i = 0;
        for (VectorEntry oe : other.fast()) {
            final long k = oe.getKey();
            while (i < domainSize && keys[i] < k) {
                i++;
            }
            if (i >= domainSize) {
                break; // no more entries
            }
            if (keys[i] == k) {
                values[i] = oe.getValue();
                usedKeys.set(i);
            } // otherwise, key is greater; advance outer 
        }
    }

    /**
     * Multiply the vector by a scalar. This multiples every element in the
     * vector by <var>s</var>.
     * @param s The scalar to rescale the vector by.
     */
    public final void scale(double s) {
        clearCachedValues();
        BitSetIterator iter = new BitSetIterator(usedKeys, 0, domainSize);
        while (iter.hasNext()) {
            int i = iter.nextInt();
            values[i] *= s;
        }
    }

    /**
     * Copy the rating vector.
     * @return A new rating vector which is a copy of this one.
     */
    public final MutableSparseVector copy() {
        return mutableCopy();
    }

    @Override
    public final MutableSparseVector mutableCopy() {
        double[] nvs = Arrays.copyOf(values, domainSize);
        BitSet nbs = (BitSet) usedKeys.clone();
        return new MutableSparseVector(keys, nvs, domainSize, nbs);
    }

    @Override
    public ImmutableSparseVector immutable() {
        return immutable(false);
    }

    /**
     * Construct an immutable sparse vector from this vector's data,
     * invalidating this vector in the process. Any subsequent use of this
     * vector is invalid; all access must be through the returned immutable
     * vector. The frozen vector's key set is equal to this vector's key domain.
     *
     * @return An immutable vector built from this vector's data.
     */
    public ImmutableSparseVector freeze() {
        return immutable(true);
    }

    private ImmutableSparseVector immutable(boolean freeze) {
        checkValid();
        ImmutableSparseVector isv;
        final int sz = size();
        if (sz == domainSize) {
            double[] nvs = freeze ? values : Arrays.copyOf(values, domainSize);
            isv = new ImmutableSparseVector(keys, nvs, domainSize);
        } else {
            long[] nkeys = new long[sz];
            double[] nvalues = new double[sz];
            int i = 0;
            int j = 0;
            while (j < sz) {
                i = usedKeys.nextSetBit(i);
                assert i >= 0; // since j < sz, this is always good!
                int k = usedKeys.nextClearBit(i);
                // number of bits to copy
                int n = k - i;
                // blit the data and advance
                System.arraycopy(keys, i, nkeys, j, n);
                System.arraycopy(values, i, nvalues, j, n);
                j += n;
                i = k;
            }
            isv = new ImmutableSparseVector(nkeys, nvalues, sz);
        }
        if (freeze) {
            values = null;
        }
        return isv;
    }

    final class IterImpl implements Iterator<VectorEntry> {
        BitSetIterator iter = new BitSetIterator(usedKeys);

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        @Nonnull
        public VectorEntry next() {
            int pos = iter.nextInt();
            return new VectorEntry(MutableSparseVector.this, pos,
                                   keys[pos], values[pos], true);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    final class FastIterImpl implements Iterator<VectorEntry> {
        VectorEntry entry = new VectorEntry(MutableSparseVector.this, -1, 0, 0, false);
        IntIterator iter;

        public FastIterImpl(IntIterator positions) {
            iter = positions;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        @Nonnull
        public VectorEntry next() {
            int pos = iter.nextInt();
            boolean set = usedKeys.get(pos);
            double v = set ? values[pos] : Double.NaN;
            entry.set(pos, keys[pos], v, set);
            return entry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Wrap key and value arrays in a sparse vector.
     *
     * <p>This method allows a new vector to be constructed from
     * pre-created arrays.  After wrapping arrays in a rating vector, client
     * code should not modify them (particularly the <var>items</var> array).
     *
     * @param keys Array of entry keys. This array must be in sorted order and
     * be duplicate-free.
     * @param values The values for the vector.
     * @return A sparse vector backed by the provided arrays.
     * @throws IllegalArgumentException if there is a problem with the provided
     * arrays (length mismatch, <var>keys</var> not sorted, etc.).
     */
    public static MutableSparseVector wrap(long[] keys, double[] values) {
        return wrap(keys, values, keys.length);
    }

    /**
     * Wrap key and value arrays in a sparse vector.
     *
     * <p>
     * This method allows a new vector to be constructed from pre-created
     * arrays. After wrapping arrays in a rating vector, client code should not
     * modify them (particularly the <var>items</var> array).
     *
     * @param keys Array of entry keys. This array must be in sorted order and
     *            be duplicate-free.
     * @param values The values for the vector.
     * @param size The size of the vector; only the first <var>size</var>
     *            entries from each array are actually used.
     * @return A sparse vector backed by the provided arrays.
     * @throws IllegalArgumentException if there is a problem with the provided
     *             arrays (length mismatch, <var>keys</var> not sorted, etc.).
     */
    public static MutableSparseVector wrap(long[] keys, double[] values, int size) {
        if (values.length < size) {
            throw new IllegalArgumentException("value array too short");
        }
        if (!MoreArrays.isSorted(keys, 0, size)) {
            throw new IllegalArgumentException("item array not sorted");
        }
        return new MutableSparseVector(keys, values, size);
    }

    /**
     * Wrap key and value array lists in a mutable sparse vector. Don't modify
     * the original lists once this has been called!
     */
    public static MutableSparseVector wrap(LongArrayList keyList, DoubleArrayList valueList) {
        if (valueList.size() < keyList.size()) {
            throw new IllegalArgumentException("Value list too short");
        }

        long[] keys = keyList.elements();
        double[] values = valueList.elements();

        if (!MoreArrays.isSorted(keys, 0, keyList.size())) {
            throw new IllegalArgumentException("key array not sorted");
        }

        return new MutableSparseVector(keys, values, keyList.size());
    }
}
