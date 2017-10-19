/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.keys;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Full 64-bit implementation of {@link SortedKeyIndex}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class FullSortedKeyIndex extends SortedKeyIndex {
    private static final long serialVersionUID = 1L;
    private final long[] keys;

    public FullSortedKeyIndex(@Nonnull long[] ks, int lb, int ub) {
        super(lb, ub);
        assert ks.length >= ub;
        keys = ks;
    }

    @Override
    public int tryGetIndex(long key) {
        return Arrays.binarySearch(keys, lowerBound, upperBound, key);
    }

    @Override
    public long getKey(int idx) {
        if (idx < lowerBound || idx >= upperBound) {
            throw new IndexOutOfBoundsException("index " + idx + " is not in range [" + lowerBound + "," + upperBound + ")");
        }
        assert idx >= lowerBound && idx < upperBound;
        return keys[idx];
    }

    @Override
    public SortedKeyIndex subIndex(int lb, int ub) {
        Preconditions.checkArgument(lb >= lowerBound && lb <= upperBound, "lower bound out of range");
        Preconditions.checkArgument(lb <= ub, "range is negative");
        Preconditions.checkArgument(ub >= lowerBound && ub <= upperBound, "upper bound out of range");
        return new FullSortedKeyIndex(keys, lb, ub);
    }
}
