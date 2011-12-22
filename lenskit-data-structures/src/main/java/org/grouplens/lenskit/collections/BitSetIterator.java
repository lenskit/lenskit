/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.ints.AbstractIntBidirectionalIterator;

import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * Iterator over the set bits in a {@link BitSet}.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
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
     * bit is the last bit returned by next(), or firstBit - 1 if next() has
     * not been called.  When previous() is called, it resets bit as if the
     * corresponding call to next() has been undone. 
     */
    private int bit;
    /*
     * nextBit is either equal to bit, lastBit, or the next set bit.
     */
    private int nextBit;

    private BitSet bitSet;

    public BitSetIterator(BitSet set) {
        this(set, 0);
    }

    public BitSetIterator(BitSet set, int start) {
        this(set, start, set.size());
    }

    /**
     * Create an iterator starting at a particular bit.
     * 
     * @param set The bit set to wrap.
     * @param start The start index, inclusive.
     * @param end The end index, exclusive.
     */
    public BitSetIterator(BitSet set, int start, int end) {
        bitSet = set;
        firstBit = start;
        bit = nextBit = start - 1;
        lastBit = end;
    }

    @Override
    public boolean hasNext() {
        if (bit == nextBit && nextBit < lastBit) {
            /* unscanned & not at end - scan for the next set bit. */
            nextBit = bitSet.nextSetBit(bit + 1);
            if (nextBit < 0) {
                nextBit = lastBit;
            }
        }
        return nextBit >= 0 && nextBit < lastBit;
    }

    @Override
    public boolean hasPrevious() {
        return bit >= firstBit && bit < lastBit;
    }

    @Override
    public int nextInt() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        bit = nextBit;
        return bit;
    }

    @Override
    public int previousInt() {
        // previous is slow
        if (bit >= firstBit && bit < lastBit) {
            int ret = bit;
            // we have successfully returned at least 1 bit
            for (int i = bit - 1; i >= firstBit; i--) {
                if (bitSet.get(i)) {
                    nextBit = bit = i;
                    break;
                }
            }
            if (bit == ret) {
                // unchanged - reset to the beginning
                nextBit = bit; // so we don't need to re-run hasNext
                bit = firstBit - 1;
            }
            return ret;
        } else {
            throw new NoSuchElementException();
        }
    }
}
