/*
 * Copyright 2011 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.grouplens.reflens.util;

import it.unimi.dsi.fastutil.longs.AbstractLongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongSortedSet;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * A sorted set of longs implemented using a sorted array.  It's much faster
 * than {@link LongArraySet} as it is able to use binary searches.  The set
 * is also immutable.
 * 
 * No orders are supported other than the natural ordering.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class LongSortedArraySet extends AbstractLongSortedSet {
	private final long[] data;
	private final int start, end;
	
	public LongSortedArraySet(Collection<Long> items) {
		this(LongIterators.unwrap(LongIterators.asLongIterator(items.iterator())));
	}
	
	/**
	 * Create a new set from an existing array.
	 * @param items An array of items. The array will be sorted and used as the
	 * backing store for the set. If this array is changed after creating the
	 * set, behavior is undefined.
	 * @see #LongSortedArraySet(long[], int, int)
	 */
	public LongSortedArraySet(long[] items) {
		this(items, 0, items.length);
	}
	
	/**
	 * Create a new set from a range of an existing array.
	 * @param items An array of items. The array will be sorted and used as the
	 * backing store for the set. If this array is changed after creating the
	 * @param fromIndex The index of the first item in the array to use.
	 * @param toIndex The end of the array to use (last index + 1).
	 * set, behavior is undefined.
	 * @throws IndexOutOfBoundsException if <var>start</var> or <var>end</var>
	 * is out of range.
	 */
	public LongSortedArraySet(long[] items, int fromIndex, int toIndex) {
		this(items, fromIndex, toIndex, false);
	}
	
	/**
	 * Create a new set from a range of an existing array.
	 * @param items An array of items. The array will be sorted and used as the
	 * backing store for the set. If this array is changed after creating the
	 * @param fromIndex The index of the first item in the array to use.
	 * @param toIndex The end of the array to use (last index + 1).
	 * set, behavior is undefined.
	 * @param clean Assume the array is sorted and has no duplicates.
	 * @throws IndexOutOfBoundsException if <var>start</var> or <var>end</var>
	 * is out of range.
	 */
	private LongSortedArraySet(long[] items, int fromIndex, int toIndex, boolean clean) {
		data = items;
		start = fromIndex;
		if (fromIndex < 0 || toIndex > data.length)
			throw new IndexOutOfBoundsException();

		if (!clean) {
			// check for sortedness first to avoid the actual sort
			if (!isSorted(data, start, toIndex))
				Arrays.sort(data, start, toIndex);
			end = deduplicate(data, start, toIndex);
		} else {
			end = toIndex;
		}
	}
	
	/**
	 * Check that the array is sorted.
	 * @return <code>true</code> iff the array is sorted.
	 */
	static boolean isSorted(final long[] data, final int start, final int end) {
		for (int i = start; i < end - 1; i++) {
			if (data[i] > data[i+1]) return false;
		}
		return true;
	}
	
	/**
	 * Remove duplicate elements in the backing store. The array should be
	 * unsorted.
	 * @return the new end index of the array
	 */
	static int deduplicate(final long[] data, final int start, final int end) {
		if (start == end) return end;   // special-case empty arrays
		
		// Since we have a non-empty array, the pos will always be where the
		// end is if we find no more unique elements.
		int pos = start + 1;
		for (int i = pos; i < end; i++) {
			if (data[i] != data[i-1]) { // we have a non-duplicate item
				if (i != pos)           // indices out of alignment, must copy
					data[pos] = data[i];
				pos++;                  // increment pos since we have a new non-dup
			}
			// if data[i] is a duplicate, then i steps forward and pos doesn't,
			// thereby arranging for data[i] to be elided.
		}
		return pos;
	}
	
	/**
	 * Find the index for a key.
	 * @see Arrays#binarySearch(long[], int, int, long)
	 * @param key
	 * @return
	 */
	private int findIndex(long key) {
		return Arrays.binarySearch(data, start, end, key);
	}
	
	/**
	 * Find the index where <var>key</var> would appear if it exists.
	 * @param key The search key.
	 * @return The index in the array of the key, if it exists; otherwise, the
	 * index of the first element greater than <var>key</var> (or the end of the
	 * array).
	 */
	private int findIndexAlways(long key) {
		int i = findIndex(key);
		if (i < 0)
			i = -(i+1);
		return i;
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.longs.LongSortedSet#comparator()
	 */
	@Override
	public LongComparator comparator() {
		return null;
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.longs.LongSortedSet#firstLong()
	 */
	@Override
	public long firstLong() {
		if (end - start > 0)
			return data[start];
		else
			throw new NoSuchElementException();
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.longs.LongSortedSet#headSet(long)
	 */
	@Override
	public LongSortedSet headSet(long key) {
		int nend = findIndexAlways(key);
		return new LongSortedArraySet(data, start, nend, true);
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.longs.LongSortedSet#iterator(long)
	 */
	@Override
	public LongBidirectionalIterator iterator(long key) {
		int index = findIndexAlways(key);
		if (index < end && data[index] == key)
			index++;
		return new IterImpl(index);
	}
	
	private final class IterImpl extends AbstractLongBidirectionalIterator {
		private int pos;
		public IterImpl(int start) {
			pos = start;
		}

		@Override
		public boolean hasNext() {
			return pos < end;
		}

		@Override
		public boolean hasPrevious() {
			return pos > start;
		}
		
		@Override
		public long nextLong() {
			if (hasNext())
				return data[pos++];
			else
				throw new NoSuchElementException();
		}
		
		@Override
		public long previousLong() {
			if (hasPrevious())
				return data[--pos];
			else
				throw new NoSuchElementException();
		}
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.longs.LongSortedSet#lastLong()
	 */
	@Override
	public long lastLong() {
		if (end - start > 0)
			return data[end-1];
		else
			throw new NoSuchElementException();
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.longs.LongSortedSet#subSet(long, long)
	 */
	@Override
	public LongSortedSet subSet(long startKey, long endKey) {
		return new LongSortedArraySet(data, findIndexAlways(startKey), findIndexAlways(endKey), true);
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.longs.LongSortedSet#tailSet(long)
	 */
	@Override
	public LongSortedSet tailSet(long key) {
		return new LongSortedArraySet(data, findIndexAlways(key), end, true);
	}

	/* (non-Javadoc)
	 * @see it.unimi.dsi.fastutil.longs.AbstractLongSortedSet#iterator()
	 */
	@Override
	public LongBidirectionalIterator iterator() {
		return new IterImpl(start);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return end - start;
	}
	
	@Override
	public boolean isEmpty() {
		return end == start;
	}
	
	@Override
	public boolean contains(long key) {
		return findIndex(key) >= 0;
	}
	
	/**
	 * Unsupported remove operation.
	 */
	@Override
	public boolean rem(long k) {
		throw new UnsupportedOperationException();
	}
}
