/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
import it.unimi.dsi.fastutil.ints.AbstractIntBidirectionalIterator;

import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * Iterator over the set bits in a {@link BitSet}, returning the indexes of the
 * set bits, in order from 0 to highest index of a set bit.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.9
 */
public final class BitSetIterator extends AbstractIntBidirectionalIterator {
    /*
     * firstBit and lastBit mark the range of this iterator, which is the
     * half-open interval [firstBit,lastBit).
     */
    private final int firstBit;
    private final int lastBit;

    /* 
     * Invariant: nextBit is the next bit to be returned by next().  If nextBit is outside of the
     * range [firstBit, lastBit), then hasNext() will be false, and nextInt() will throw an exception.
     * 
     * previousInt() will walk backwards from nextBit to the previous set bit, return it, and
     * set the nextBit to that bit.
     * 
     * If nextBit is run out of the range using nextInt(), it will have the value lastBit.  If
     * nextBit is run out of the range using previousInt(), it will have the value -1.
     */
    private int nextBit;

    /* 
     * The BitSet we are iterating over.
     */
    private BitSet bitSet;

    /**
     * Construct an iterator over a bit set.
     *
     * @param set The set to iterate.
     */
    public BitSetIterator(BitSet set) {
        this(set, 0);
    }

    /**
     * Construct an iterator over a bit set, starting at a desired index.
     *
     * @param set   The set to iterate.
     * @param start The first bit to return.
     */
    public BitSetIterator(BitSet set, int start) {
        this(set, start, set.size());
    }

    /**
     * Create an iterator starting at a particular bit and ending at another index.
     * The indices returned are inclusive of the starting index, and exclusive of the ending index.
     *
     * @param set   The bit set to wrap.
     * @param start The start index, inclusive.
     * @param end   The end index, exclusive.
     */
    public BitSetIterator(BitSet set, int start, int end) {
        this(set, start, end, start);
    }

    /**
     * Create an iterator starting at a particular bit and ending at another index, with an initial
     * position that may not be at the beginning.  The indices returned are inclusive of the
     * starting index, and exclusive of the ending index.
     *
     * @param set   The bit set to wrap.
     * @param start The start index, inclusive.
     * @param end   The end index, exclusive.
     * @param initial The initial position of the iterator.  The first call to {@link #nextInt()}
     *                returns the first set bit such that {@code bit >= initial} and
     *                {@code bit < end}.
     */
    public BitSetIterator(BitSet set, int start, int end, int initial) {
        Preconditions.checkArgument(start >= 0, "Starting index must be non-negative");
        Preconditions.checkArgument(start <= end, "Starting index must not be past ending index");
        Preconditions.checkArgument(initial >= start, "initial index must be >= start");
        Preconditions.checkArgument(initial <= end, "initial index must be <= end");
        bitSet = set;
        firstBit = start;
        lastBit = end;
        nextBit = bitSet.nextSetBit(initial);
        if (nextBit < 0) {
            nextBit = lastBit;
        }
    }

    @Override
    public boolean hasNext() {
        // The current implementation of the invariant never allows nextBit to be
        // less than firstBit, so the following line cannot be completely tested.
        return nextBit >= firstBit && nextBit < lastBit;
    }

    /*
     * Given a starting index, return the previously set bit, or -1 if there is no
     * previously set bit in the bit set.  Note that the common use will want to
     * call previousIndex(nextBit - 1), since nextBit is the *next* bit, not
     * the previous bit.
     */
    private int previousSetBit(int start) {
        for (int i = start; i >= firstBit; i--) {
            if (bitSet.get(i)) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public boolean hasPrevious() {
        return previousSetBit(nextBit - 1) >= firstBit;
    }

    @Override
    public int nextInt() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int retval = nextBit;
        nextBit = bitSet.nextSetBit(nextBit + 1);
        if (nextBit < 0) {
            nextBit = lastBit;
        }
        return retval;
    }

    @Override
    public int previousInt() {
        int prevBit = previousSetBit(nextBit - 1);
        if (prevBit < 0) {
            throw new NoSuchElementException();
        } 
        nextBit = prevBit;
        return nextBit;
    }
}
