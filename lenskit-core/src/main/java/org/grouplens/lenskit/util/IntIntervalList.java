package org.grouplens.lenskit.util;

import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntListIterator;

/**
 * Efficient representation of intervals as an integer list.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IntIntervalList extends AbstractIntList {
	private final int start;
	private final int end;
	
	/**
	 * Create the half-open interval [0,size).
	 * @param size The size of the interval.
	 */
	public IntIntervalList(int size) {
		this(0, size);
	}
	
	private static void checkIndex(int idx, int start, int end) {
		if (idx < 0 || start + idx >= end)
			throw new IndexOutOfBoundsException(String.format("%d not in [%d,%d)", idx, start, end));
	}
	
	/**
	 * Create the half-open interval [start,end).
	 * @param start The interval start point (inclusive).
	 * @param end The interval end point (exclusive).
	 */
	public IntIntervalList(int start, int end) {
		if (end < start)
			throw new IllegalArgumentException("end < start");
		this.start = start;
		this.end = end;
	}

	@Override
	public int getInt(int index) {
		checkIndex(index, start, end);
		return start + index;
	}

	@Override
	public int size() {
		return end - start;
	}
	
	/**
	 * Use {@link IntIterators#fromTo(int, int)} to build an iterator.  The other
	 * iterator methods in {@link AbstractIntList} delegate to this one, so this
	 * is the good injection point.
	 */
	@Override
	public IntListIterator listIterator(int idx) {
		checkIndex(idx, start, end + 1); // this index can be one past the end
		return IntIterators.fromTo(start + idx, end);
	}
}
