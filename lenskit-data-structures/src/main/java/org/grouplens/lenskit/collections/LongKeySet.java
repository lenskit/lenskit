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
import java.util.NoSuchElementException;

/**
 * Implement a domain of long keys, sorted by key.  Keys can be mapped back to indexes and vice
 * versa; it has a fixed domain, and a bitmask of active entries.  This is a helper class for
 * implementing {@link LongSortedArraySet}, {@link org.grouplens.lenskit.vectors.SparseVector}, etc.
 * This class should not be directly used outside of the LensKit data structures.
 * <p>
 * A key set has a <emph>domain</emph>, which is the set of all possible keys that it can contain.
 * These keys are stored in an array.  The <emph>active</emph> keys are those that are actually in
 * the set.  Active/inactive status is tracked with a bitmask.
 * <p>
 * Indexes are not necessarily 0-based; they start from {@link #getStartIndex()}.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Private
 */
public final class LongKeySet implements Serializable {
    /**
     * Wrap a key array (with a specified size) into a key set.  The key set is initially empty
     * (the mask is clear).
     * @param keys The key array.
     * @param fromIndex The index of the first key to use.
     * @param toIndex The index of the last key to use.
     * @param initiallyActive {@code true} to activate all keys initially, {@code false} to leave them
     *                                 inactive.
     * @return The key set.
     */
    public static LongKeySet wrap(long[] keys, int fromIndex, int toIndex, boolean initiallyActive) {
        Preconditions.checkArgument(fromIndex >= 0, "invalid starting index");
        Preconditions.checkArgument(toIndex >= fromIndex, "toIndex less than fromIndex");
        Preconditions.checkArgument(toIndex <= keys.length, "toIndex past end of array");
        assert MoreArrays.isSorted(keys, fromIndex, toIndex);
        BitSet mask = new BitSet(toIndex);
        if (initiallyActive) {
            mask.set(fromIndex, toIndex);
        }
        return new LongKeySet(keys, fromIndex, toIndex, mask);
    }

    /**
     * Create a key set from a collection of keys.
     *
     * @param keys            The key collection.
     * @param initiallyActive {@code true} if the elements of the key set should be initially
     *                        activated; {@code false} to make all keys initially inactive.
     * @return The key set.
     */
    public static LongKeySet fromCollection(Collection<Long> keys, boolean initiallyActive) {
        if (keys instanceof LongSortedArraySet) {
            return ((LongSortedArraySet) keys).getKeySet().compactCopy(initiallyActive);
        }

        long[] keyArray;
        if (keys instanceof LongCollection) {
            keyArray = ((LongCollection) keys).toLongArray();
        } else {
            keyArray = LongIterators.unwrap(LongIterators.asLongIterator(keys.iterator()));
        }
        Arrays.sort(keyArray);
        int size = MoreArrays.deduplicate(keyArray, 0, keyArray.length);
        return wrap(keyArray, 0, size, initiallyActive);
    }

    /**
     * Create a key set from a collection of keys.  All keys are initially active.
     * @param keys The keys.
     * @return The key set.
     */
    public static LongKeySet fromCollection(Collection<Long> keys) {
        return fromCollection(keys, true);
    }

    /**
     * Create a key set with some keys.  All keys are initially active.
     * @param keys The keys.
     * @return The key set.
     */
    public static LongKeySet create(long... keys) {
        return fromCollection(LongArrayList.wrap(keys));
    }

    /**
     * Create an empty key set.
     * @return An empty key set.
     */
    public static LongKeySet empty() {
        return wrap(new long[0], 0, 0, true);
    }

    private static final long serialVersionUID = 1L;

    private final long[] keys;
    private final int startIndex;
    private final int endIndex;
    private final BitSet mask;
    private boolean unowned = false;

    private LongKeySet(long[] ks, int start, int end, BitSet m) {
        keys = ks;
        startIndex = start;
        endIndex = end;
        mask = m;
    }

    /**
     * Get the start index for this set.  This is the lower bound on indexes that can be returned.
     * @return The start index.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Get the end index for this set.  This is the upper bound on indexes that can be returned.
     * @return The end index.
     */
    public int getEndIndex() {
        return endIndex;
    }

    private void checkIndex(int idx) {
        if (idx < startIndex || idx >= endIndex) {
            String msg = String.format("index %d not in range [%d,%d)", idx, startIndex, endIndex);
            throw new IndexOutOfBoundsException(msg);
        }
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
        return Arrays.binarySearch(keys, startIndex, endIndex, key);
    }

    /**
     * Get the index for a key if that key is active.
     * @param key The key.
     * @return The index, or a negative value if the key is not in the domain or is inactive.
     */
    public int getIndexIfActive(long key) {
        int idx = getIndex(key);
        if (idx >= 0) {
            if (mask.get(idx)) {
                return idx;
            } else {
                return -idx - 1;
            }
        } else {
            return idx;
        }
    }

    /**
     * Get the upper bound, the first index whose key is greater than the specified key.
     * @param key The key to search for.
     * @return The first index greater than the specified key, or {@link #size()} if the key is
     *            the last key in the domain.  The index is not necessarily active.
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
     * Return a subset of this key set.  The masks of the two sets <strong>are linked</strong>.
     * @param start The start index (inclusive).
     * @param end The end index (exclusive).
     * @return A key set representing a subset of this set.
     */
    public LongKeySet subset(int start, int end) {
        Preconditions.checkArgument(start >= startIndex, "invalid start index");
        Preconditions.checkArgument(end <= endIndex, "invalid end index");
        Preconditions.checkArgument(end >= start, "end before start");
        return new LongKeySet(keys, start, end, mask);
    }

    /**
     * Return a copy of this key set.  The resulting key set has an independent mask.  Key storage
     * is shared for efficiency.
     * @return The copied key set.
     */
    @Override
    public LongKeySet clone() {
        if (unowned) {
            unowned = false;
            return this;
        } else {
            return new LongKeySet(keys, startIndex, endIndex, (BitSet) mask.clone());
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
    public LongKeySet unowned() {
        unowned = true;
        return this;
    }

    /**
     * Mark the key set as owned, but don't copy it.  Used by views to make sure that someone owns
     * the key set.
     */
    public void requireOwned() {
        unowned = false;
    }

    /**
     * Return a copy of this key set that is entirely inactive.
     * @return The new key set, with the same keys but all of them deactivated.
     */
    public LongKeySet inactiveCopy() {
        return new LongKeySet(keys, startIndex, endIndex, new BitSet());
    }

    /**
     * Make a compact copy of this key set. In a compact copy, the key array has no extra storage
     * and only the active keys are retained.  All keys are active in the resulting set.
     * @return A compacted copy of this key set.
     */
    public LongKeySet compactCopy() {
        return compactCopy(true);
    }

    /**
     * Make a compact copy of this key set. In a compact copy, the key array has no extra storage
     * and only the active keys are retained.
     * @param active Whether the keys should be active or inactive in the compacted key set.
     * @return A compacted copy of this key set.
     */
    public LongKeySet compactCopy(boolean active) {
        long[] compactKeys;
        if (startIndex == 0 && endIndex == keys.length && mask.nextClearBit(0) >= endIndex) {
            // fast path 1: reuse the keys
            compactKeys = keys;
        } else if (mask.nextClearBit(startIndex) >= endIndex) {
            // fast path 2: all keys are active, use fast copy
            int size = domainSize();
            compactKeys = new long[size];
            System.arraycopy(keys, startIndex, compactKeys, 0, size);
        } else {
            // there are unused keys, do a slow copy
            compactKeys = LongIterators.unwrap(keyIterator(activeIndexIterator()));
            assert compactKeys.length == size();
        }

        BitSet compactMask = new BitSet(compactKeys.length);
        if (active) {
            compactMask.set(0, compactKeys.length, true);
        }
        return new LongKeySet(compactKeys, 0, compactKeys.length, compactMask);
    }

    /**
     * Query whether an index is active.
     * @param idx The index.
     * @return {@code true} if the key at the index is active.
     */
    public boolean indexIsActive(int idx) {
        checkIndex(idx);
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
        checkIndex(idx);
        return keys[idx];
    }

    /**
     * Get the first active key.
     * @return The first active key.
     */
    public long firstActiveKey() {
        int idx = mask.nextSetBit(startIndex);
        if (idx < endIndex) {
            return keys[idx];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Get the first active key.
     * @return The last active key.
     */
    public long lastActiveKey() {
        int idx = mask.previousSetBit(endIndex - 1);
        if (idx >= startIndex) {
            return keys[idx];
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Get the domain size of this set.
     * @return The domain size.
     */
    public int domainSize() {
        return endIndex - startIndex;
    }

    /**
     * Get the number of active keys in this set.
     * @return The number of active keys.
     */
    public int size() {
        BitSet bits = new BitSet(endIndex + 1);
        bits.set(startIndex, endIndex);
        bits.and(mask);
        return bits.cardinality();
    }

    /**
     * Get an iterator over active indexes.
     * @return An iterator over active indexes.
     */
    public IntBidirectionalIterator activeIndexIterator() {
        // shortcut - only iterate the bit set if it has clear bits
        if (mask.nextClearBit(startIndex) < endIndex) {
            return new BitSetIterator(mask, startIndex, endIndex);
        } else {
            return IntIterators.fromTo(startIndex, endIndex);
        }
    }

    /**
     * Get a pointer over active indexes.
     * @return A pointer over the active indexes.
     */
    public IntPointer activeIndexPointer() {
        // shortcut - only iterate the bit set if it has clear bits
        if (mask.nextClearBit(startIndex) < endIndex) {
            return new BitSetPointer(mask, startIndex, endIndex);
        } else {
            return Pointers.fromTo(startIndex, endIndex);
        }
    }

    /**
     * Get an iterator over active indexes, initialized to the specified index.
     * @param idx The starting index for the iterator.  The iterator can go backwards from this
     *            index, if it is greater than 0.
     * @return An iterator over active indexes.
     */
    public IntBidirectionalIterator activeIndexIterator(int idx) {
        // shortcut - only iterate the bit set if it has clear bits
        if (mask.nextClearBit(startIndex) < endIndex) {
            return new BitSetIterator(mask, startIndex, endIndex, idx);
        } else {
            IntBidirectionalIterator iter = IntIterators.fromTo(startIndex, endIndex);
            iter.skip(idx - startIndex);
            return iter;
        }
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
    public LongSortedSet asSet() {
        return new LongSortedArraySet(this);
    }

    /**
     * Get the key set's domain as a set.
     * @return A view of the key domain as a set.
     */
    public LongSortedSet domain() {
        // TODO Cache the domain
        BitSet bits = new BitSet(endIndex);
        bits.set(startIndex, endIndex);
        return new LongSortedArraySet(new LongKeySet(keys, startIndex, endIndex, bits));
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
     * compatible.  Key sets generated with {@link #subset(int, int)} and {@link #clone()} are
     * compatible with their parent and each other.
     *
     * @param other The other key set.
     * @return {@code true} if the two key sets are compatible.
     */
    public boolean isCompatibleWith(@Nonnull LongKeySet other) {
        return keys == other.keys;
    }

    //region Active flag modification
    /**
     * Invert the active status of all keys in the set.  The set is modified in place; it is just
     * returned for chaining.
     * @return {@code this} (for chaining).
     */
    public LongKeySet invert() {
        mask.flip(startIndex, endIndex);
        return this;
    }

    /**
     * Set the active status of all entries in the key set.
     * @param active {@code true} to activate, {@code false} to deactivate.
     * @return The key set (for chaining).
     */
    public LongKeySet setAllActive(boolean active) {
        mask.set(startIndex, endIndex, active);
        return this;
    }

    /**
     * Set the active flag for a single key.
     * @param idx The key's index.
     * @param active Whether the key is active.
     * @return The key set (for chaining).
     */
    public LongKeySet setActive(int idx, boolean active) {
        checkIndex(idx);
        mask.set(idx, active);
        return this;
    }

    /**
     * Set the active bits from a bit set.
     * @param active The bits to set.  The bit set should be in the key set's index-space (that is,
     *               it will be queried starting from {@link #getStartIndex()}, not 0).
     * @return The key set (for chaining).
     */
    public LongKeySet setActive(BitSet active) {
        mask.set(startIndex, endIndex);
        mask.and(active);
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
}
