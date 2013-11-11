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
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.longs.*;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;

/**
 * Implement a domain of long keys, sorted by key.  Keys can be mapped back to indexes and vice
 * versa; it has a fixed domain, and a bitmask of active entries.  This is a helper class for
 * implementing {@link LongSortedArraySet}, {@link org.grouplens.lenskit.vectors.SparseVector}, etc.
 * This class should not be directly used outside of the LensKit data structures.
 * <p>
 * A key set has a <emph>domain</emph>, which is the set of all possible keys that it can contain.
 * These keys are stored in an array.  The <emph>active</emph> keys are those that are actually in
 * the set.  Active/inactive status is tracked with a bitmask.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Private
 */
@SuppressWarnings("deprecation")
public final class LongKeyDomain implements Serializable {
    /**
     * Wrap a key array (with a specified size) into a key set.
     * @param keys The key array.  This array must be sorted, and must not contain duplicates.  For
     *             efficiency, this condition is not checked unless assertions are enabled.  Since
     *             this method is only intended to be used when implementing test cases or other
     *             data structures, callers of this method should ensure sortedness and
     *             throw the appropriate exception.
     * @param size The length of the array to actually use.
     * @param initiallyActive {@code true} to activate all keys initially, {@code false} to leave
     *                        them inactive.
     * @return The key set.
     */
    public static LongKeyDomain wrap(long[] keys, int size, boolean initiallyActive) {
        Preconditions.checkArgument(size <= keys.length, "size too large");
        assert MoreArrays.isSorted(keys, 0, size);
        BitSet mask = new BitSet(size);
        if (initiallyActive) {
            mask.set(0, size);
        }
        return new LongKeyDomain(keys, size, mask);
    }

    /**
     * Create a key set from a collection of keys.
     *
     * @param keys            The key collection.
     * @param initiallyActive {@code true} if the elements of the key set should be initially
     *                        activated; {@code false} to make all keys initially inactive.
     * @return The key set.
     */
    public static LongKeyDomain fromCollection(Collection<Long> keys, boolean initiallyActive) {
        if (keys instanceof LongSortedArraySet) {
            return ((LongSortedArraySet) keys).getDomain().compactCopy(initiallyActive);
        }

        long[] keyArray;
        if (keys instanceof LongCollection) {
            keyArray = ((LongCollection) keys).toLongArray();
        } else {
            keyArray = LongIterators.unwrap(LongIterators.asLongIterator(keys.iterator()));
        }
        Arrays.sort(keyArray);
        int size = MoreArrays.deduplicate(keyArray, 0, keyArray.length);
        return wrap(keyArray, size, initiallyActive);
    }

    /**
     * Create a key set from a collection of keys.  All keys are initially active.
     * @param keys The keys.
     * @return The key set.
     */
    public static LongKeyDomain fromCollection(Collection<Long> keys) {
        return fromCollection(keys, true);
    }

    /**
     * Create a key set with some keys.  All keys are initially active.
     * @param keys The keys.
     * @return The key set.
     */
    public static LongKeyDomain create(long... keys) {
        // the delegation goes this way to minimize the number of array copies
        return fromCollection(LongArrayList.wrap(keys));
    }

    /**
     * Create an empty key domain.
     * @return An empty key domain.
     */
    public static LongKeyDomain empty() {
        // since empty domains are immutable, use a singleton
        return EMPTY_DOMAIN;
    }

    private static final LongKeyDomain EMPTY_DOMAIN = wrap(new long[0], 0, true);

    private static final long serialVersionUID = 1L;

    private final long[] keys;
    private final int domainSize;
    private final BitSet mask;
    private boolean unowned = false;

    private LongKeyDomain(long[] ks, int end, BitSet m) {
        keys = ks;
        domainSize = end;
        mask = m;
    }

    /**
     * Get the index for a key, regardless of its active state.
     *
     * @param key The key.
     * @return The index, or a negative value if the key is not in the domain.  Such a negative
     *         value is the <emph>insertion point</emph>, as defined by
     *         {@link Arrays#binarySearch(long[], int, int, long)}.
     */
    public int getIndex(long key) {
        return Arrays.binarySearch(keys, 0, domainSize, key);
    }

    /**
     * Get the index for a key if that key is active.
     * @param key The key.
     * @return The index, or a negative value if the key is not in the domain or is inactive.
     */
    public int getIndexIfActive(long key) {
        int idx = getIndex(key);
        if(idx >= 0 && !mask.get(idx)) {
            return -idx - 1;
        } else {
            // index is negative or active
            return idx;
        }
    }

    /**
     * Get the upper bound, the first index whose key is greater than the specified key.
     * @param key The key to search for.
     * @return The first index greater than the specified key, or {@link #domainSize()} if the key
     *         is the last key in the domain.  The index is not necessarily active.
     */
    public int upperBound(long key) {
        int index = getIndex(key);
        if (index >= 0) {
            // the key is there, advance by 1
            return index + 1;
        } else {
            // the key is not there, the insertion point is > key
            return -index - 1;
        }
    }

    /**
     * Get the lower bound, the first index whose key is greater than or equal to the specified key.
     * This method is paired with {@link #upperBound(long)}; the interval
     * {@code [lowerBound(k),upperBound(k))} contains the index of {@code k}, if the key is in the
     * domain, and is empty if the key is not in the domain.
     * @param key The key to search for.
     * @return The index of the first key greater than or equal to {@code key}.
     */
    public int lowerBound(long key) {
        int index = getIndex(key);
        if (index >= 0) {
            // the key is there, first index is >=
            return index;
        } else {
            // the key is not there, the insertion point is > key
            return -index - 1;
        }
    }

    /**
     * Return a copy of this key set.  The resulting key set has an independent mask.  Key storage
     * is shared for efficiency.
     * @return The copied key set.
     */
    @Override
    public LongKeyDomain clone() {
        if (unowned) {
            unowned = false;
            return this;
        } else {
            return new LongKeyDomain(keys, domainSize, (BitSet) mask.clone());
        }
    }

    /**
     * Mark this key set as <emph>unowned</emph>.  The next call to {@link #clone()} will mark the
     * key set as owned and return it rather than making a copy.  This allows code to avoid an
     * extra copy when creating a key set to pass off to another method or object that will make
     * a defensive copy.
     * <p>You almost certainly do not want to call this method.
     * <p>Any object or method that receives a key set that it intends to take ownership of must
     * call {@link #clone()} to make sure that it owns the set.
     * @return This key set (for chaining).
     */
    public LongKeyDomain unowned() {
        unowned = true;
        return this;
    }

    /**
     * Mark the key set as owned, but don't copy it.  Used by views to make sure that someone owns
     * the key set.
     */
    public void acquire() {
        unowned = false;
    }

    /**
     * Return a copy of this key set that is entirely inactive.
     * @return The new key set, with the same keys but all of them deactivated.
     */
    public LongKeyDomain inactiveCopy() {
        return new LongKeyDomain(keys, domainSize, new BitSet());
    }

    /**
     * Make a compact copy of this key set. In a compact copy, the key array has no extra storage
     * and only the active keys are retained.  All keys are active in the resulting set.
     * @return A compacted copy of this key set.
     */
    public LongKeyDomain compactCopy() {
        return compactCopy(true);
    }

    /**
     * Make a compact copy of this key set. In a compact copy, the key array has no extra storage
     * and only the active keys are retained.
     * @param active Whether the keys should be active or inactive in the compacted key set.
     * @return A compacted copy of this key set.
     */
    public LongKeyDomain compactCopy(boolean active) {
        long[] compactKeys;
        if (domainSize == keys.length && mask.nextClearBit(0) >= domainSize) {
            // fast path 1: reuse the keys
            compactKeys = keys;
        } else if (mask.nextClearBit(0) >= domainSize) {
            // fast path 2: all keys are active, use fast copy
            int size = domainSize();
            compactKeys = new long[size];
            System.arraycopy(keys, 0, compactKeys, 0, size);
        } else {
            // there are unused keys, do a slow copy
            compactKeys = LongIterators.unwrap(keyIterator(activeIndexIterator(false)));
            assert compactKeys.length == size();
        }

        BitSet compactMask = new BitSet(compactKeys.length);
        if (active) {
            compactMask.set(0, compactKeys.length, true);
        }
        return new LongKeyDomain(compactKeys, compactKeys.length, compactMask);
    }

    /**
     * Query whether an index is active.
     * @param idx The index.
     * @return {@code true} if the key at the index is active.
     */
    public boolean indexIsActive(int idx) {
        assert idx >= 0 && idx < domainSize;
        return mask.get(idx);
    }

    /**
     * Query whether a key is active.
     * @param key The key to query.
     * @return {@code true} if the key is in the domain and active.
     */
    public boolean keyIsActive(long key) {
        return getIndexIfActive(key) >= 0;
    }

    /**
     * Query whether this set contains the specified key in its domain.
     * @param key The key.
     * @return {@code true} if the key is in the domain.
     */
    public boolean containsKey(long key) {
        return getIndex(key) >= 0;
    }

    /**
     * Get the key at an index.
     * @param idx The index to query.
     * @return The key at the specified index.
     */
    public long getKey(int idx) {
        assert idx >= 0 && idx < domainSize;
        return keys[idx];
    }

    /**
     * Get the domain size of this set.
     * @return The domain size.
     */
    public int domainSize() {
        return domainSize;
    }

    /**
     * Get the number of active keys in this set.
     * @return The number of active keys.
     */
    public int size() {
        return mask.cardinality();
    }

    /**
     * Get an iterator over active indexes.
     * @param mayBeModified Whether the set's active/inactive flags may be modified during iteration.
     *                      If {@code false}, this method is slightly more efficient; if {@code true},
     *                      the iterator will iterate over a snapshot of the current active/inactive
     *                      state.
     * @return An iterator over active indexes.
     */
    public IntBidirectionalIterator activeIndexIterator(boolean mayBeModified) {
        // shortcut - only iterate the bit set if it has clear bits
        if (mask.nextClearBit(0) < domainSize) {
            BitSet snap = mask;
            if (mayBeModified) {
                snap = (BitSet) snap.clone();
            }
            return new BitSetIterator(snap, 0, domainSize);
        } else {
            return IntIterators.fromTo(0, domainSize);
        }
    }

    /**
     * Get an iterator over active indexes, initialized to the specified index and limited to a
     * particular range.
     * @param min The minimum index for the iterator.
     * @param max The maximum index for the iterator.
     * @param idx The starting index for the iterator.  The iterator can go backwards from this
     *            index, if it is greater than {@code min}.
     * @return An iterator over active indexes.
     */
    public IntBidirectionalIterator activeIndexIterator(int min, int max, int idx) {
        assert min >= 0;
        assert max <= domainSize;
        assert idx >= min && idx <= max;
        return new BitSetIterator(mask, min, max, idx);
    }

    /**
     * Wrap an index iterator into a key iterator.
     * @param iter The index iterator.
     * @return An iterator over the keys corresponding to the iterator's indexes.
     */
    public LongBidirectionalIterator keyIterator(IntBidirectionalIterator iter) {
        return new KeyIter(iter);
    }

    /**
     * Get a vew of this key set as a set.
     * @return A view of the active keys as a set.
     */
    @SuppressWarnings("deprecation")
    public LongSortedSet activeSetView() {
        return new LongSortedArraySet(this);
    }

    /**
     * Get a view of this key set as a set that supports limited mutation.  The set can have items
     * removed (using methods such as {@code remove(Object)}, {@code rem(long)},
     * {@code removeAll(Collection)}, and {@code retainAll(Collection)}), and those removals will
     * be reflected by marking the associated entries as inactive in the key set.
     * @return A view of this key set as a set with limited mutation capabilities.
     * @see #activeSetView()
     */
    public LongSortedSet modifiableActiveSetView() {
        return new RemovableLongSortedArraySet(this);
    }

    /**
     * Get the key set's domain as a set.
     * @return A view of the key domain as a set.
     */
    @SuppressWarnings("deprecation")
    public LongSortedSet domain() {
        // TODO Cache the domain
        BitSet bits = new BitSet(domainSize);
        bits.set(0, domainSize);
        return new LongSortedArraySet(new LongKeyDomain(keys, domainSize, bits));
    }

    /**
     * Get the key set's list of keys (domain) as a list.
     * @return A list of all keys in the key domain.
     */
    public LongList keyList() {
        return new KeyList();
    }

    /**
     * Get the active keys as a bit set.  The returned bit set <strong>must not</strong> be modified.
     */
    public BitSet getActiveMask() {
        return mask;
    }

    /**
     * Query whether this key set is compatible with another.  Two key sets are compatible if indexes
     * are compatible (that is, the same index will refer to the same key).  This method is
     * conservative and efficient; it may claim that two sets are incompatible even if indexes may
     * compatible.  Key sets generated with {@link #clone()} are compatible with their parent and
     * each other.
     *
     * @param other The other key set.
     * @return {@code true} if the two key sets are compatible.
     */
    public boolean isCompatibleWith(@Nonnull LongKeyDomain other) {
        return keys == other.keys;
    }

    //region Active flag modification
    /**
     * Invert the active status of all keys in the set.  The set is modified in place; it is just
     * returned for chaining.
     * @return {@code this} (for chaining).
     */
    public LongKeyDomain invert() {
        mask.flip(0, domainSize);
        return this;
    }

    /**
     * Set the active status of all entries in the key set.
     * @param active {@code true} to activate, {@code false} to deactivate.
     * @return The key set (for chaining).
     */
    public LongKeyDomain setAllActive(boolean active) {
        mask.set(0, domainSize, active);
        return this;
    }

    /**
     * Set the active flag for a single key.
     * @param idx The key's index.
     * @param active Whether the key is active.
     * @return The key set (for chaining).
     */
    public LongKeyDomain setActive(int idx, boolean active) {
        Preconditions.checkElementIndex(idx, domainSize);
        mask.set(idx, active);
        return this;
    }

    /**
     * Set the active bits from a bit set.
     * @param active The bits to set.  Unset bits in this set are cleared.
     * @return The key domain (for chaining).
     */
    public LongKeyDomain setActive(BitSet active) {
        mask.set(0, domainSize);
        mask.and(active);
        return this;
    }

    /**
     * Activate bits from a bit set.
     * @param active The bits to set. Unset bits in this set are left unchanged.
     * @return The key domain (for chaining).
     */
    public LongKeyDomain activate(BitSet active) {
        mask.or(active);
        return this;
    }
    //endregion

    private class KeyIter extends AbstractLongBidirectionalIterator {
        private final IntBidirectionalIterator delegate;

        public KeyIter(IntBidirectionalIterator iter) {
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
        public long nextLong() {
            return getKey(delegate.nextInt());
        }

        @Override
        public long previousLong() {
            return getKey(delegate.previousInt());
        }
    }

    private class KeyList extends AbstractLongList {
        @Override
        public int size() {
            return domainSize();
        }

        @Override
        public long getLong(int i) {
            Preconditions.checkElementIndex(i, domainSize());
            return getKey(i);
        }
    }
}
