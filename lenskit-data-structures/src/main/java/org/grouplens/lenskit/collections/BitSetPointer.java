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

import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * A pointer over the bits in a bit set.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Deprecated
public final class BitSetPointer extends AbstractIntPointer {
    private final BitSet bitSet;
    // position is the current bit, or < 0 if past the end
    private int position;
    private final int limit;

    /**
     * Create a bit set pointer starting at the first set bit.
     * @param bits The bit set.
     */
    public BitSetPointer(BitSet bits) {
        this(bits, 0);
    }

    public BitSetPointer(BitSet bits, int start) {
        this(bits, start, bits.size());
    }

    /**
     * Create a new bit set pointer with a specified starting position.  The pointer is at the
     * first set bit at or after the specified starting point, or at the end if there is no such bit.
     * @param bits The bit set to iterate over.
     * @param start The starting index.
     * @param limit The upper limit (exclusive) of the bits to return.
     */
    public BitSetPointer(BitSet bits, int start, int limit) {
        bitSet = bits;
        position = bitSet.nextSetBit(start);
        this.limit = limit;
    }

    @Override
    public int getInt() {
        if (position >= 0 && position < limit) {
            return position;
        } else {
            throw new NoSuchElementException("bit set pointer out of bounds");
        }
    }

    @Override
    public boolean advance() {
        if (position >= 0 && position < limit) {
            position = bitSet.nextSetBit(position + 1);
        }
        return position >= 0 && position < limit;
    }

    @Override
    public boolean isAtEnd() {
        return position < 0 || position >= limit;
    }
}
