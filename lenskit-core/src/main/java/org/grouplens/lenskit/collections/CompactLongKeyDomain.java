/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Compact implementation of {@link LongKeyDomain}.  This implementation stores the data as ints,
 * saving a lot of memory.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class CompactLongKeyDomain extends LongKeyDomain {
    private static final long serialVersionUID = 1L;
    private final int[] keys;

    public CompactLongKeyDomain(@Nonnull int[] ks, int size, BitSet m) {
        super(size, m);
        assert ks.length >= size;
        keys = ks;
    }

    public int getIndex(long key) {
        // this domain does not contain anything outside the range of integers
        if (key > Integer.MAX_VALUE || key < Integer.MIN_VALUE) {
            return -1;
        } else {
            return Arrays.binarySearch(keys, 0, domainSize, (int) key);
        }
    }

    @Override
    public long getKey(int idx) {
        assert idx >= 0 && idx < domainSize;
        return keys[idx];
    }

    @Override
    public boolean isCompatibleWith(@Nonnull LongKeyDomain other) {
        return other instanceof CompactLongKeyDomain && keys == ((CompactLongKeyDomain) other).keys;
    }

    @Override
    protected LongKeyDomain makeClone(BitSet mask) {
        return new CompactLongKeyDomain(keys, domainSize, mask);
    }

    @Override
    LongKeyDomain makeCompactCopy(BitSet m) {
        if (domainSize == keys.length) {
            return new CompactLongKeyDomain(keys, domainSize, m);
        } else {
            return new CompactLongKeyDomain(Arrays.copyOf(keys, domainSize), domainSize, m);
        }
    }
}
