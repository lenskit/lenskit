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
package org.lenskit.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for implementing and working with binary searches.  Binary searches over arbitrary structures can
 * be performed by subclassing this class.  Instances must capture the target (the key to be searched for), there is
 * not a means of providing the key at search time.
 */
public abstract class BinarySearch {
    /**
     * Perform a binary search across a range of integers.  This is much like {@link Arrays#binarySearch(Object[], Object)},
     * but allows for searching over arbitrary indexed structures, and has defined behavior when the input sequence
     * contains repeated elements (it always finds the first).
     *
     * The efficiency guarantees of this method assume that there are not very many objects equal to the target element,
     * as it does a linear scan to find the first once it finds one.
     *
     * @param start The lower bound of the binary search (inclusive).  Must be nonnegative.
     * @param end The upper bound of the binary search (exclusive). Must be no less than `start`.
     * @return The position in the range [start,end) where the target is found.  If the target is not found, then the
     * value \\(-i-1\\) is returned, where \\(i\\) is the *insertion point*, the point at which the target would be
     * inserted to appear in sorted order (the index of the first entry greater than the target, or `end` if the target
     * is greater than all members).  If multiple items match the target, this method is guaranteed to return the index
     * of the *first* (unlike {@link java.util.Arrays#binarySearch(Object[], Object)}).
     * @throws IllegalArgumentException if `start` or `end` is out of range.
     */
    public int search(int start, int end) {
        Preconditions.checkArgument(start >= 0, "Start position is negative");
        Preconditions.checkArgument(end >= start, "End is before start");
        if (end == start) {
            return -end - 1;
        } else {
            int off = (end - start) / 2;
            int pos = start + off;
            int cmp = test(pos);
            if (cmp < 0) {
                // target less than pos, go left
                return search(start, pos);
            } else if (cmp > 0) {
                // target more than pos, go right
                return search(pos + 1, end);
            } else {
                // we found it - walk backwards to first
                while (pos > 0 && test(pos-1) == 0) {
                    pos -= 1;
                }
                return pos;
            }
        }
    }

    /**
     * Test the value at position `pos`, comparing it against the target.
     *p
     * To understand this method, suppose that an array `objs` of {@link Comparable} objects is being searched. Then
     * calling this method is equivalent to:
     *
     * ```
     * target.compareTo(objs[pos])
     * ```
     *
     * @return A negative value if the target is less than the value at position `pos`, positive if it is greater, and
     * 0 if the objects should be considered equal.
     */
    protected abstract int test(int pos);

    /**
     * Create a search over a list.
     * @param needle The item to search for.
     * @param haystack The list to search
     * @param <E> The element type.
     * @return A binary search that, when searched, will search for `needle` in `haystack`.
     */
    public static <E extends Comparable<? super E>> BinarySearch forList(@Nonnull final E needle,
                                                                         @Nonnull final List<? extends E> haystack) {
        return new BinarySearch() {
            @Override
            protected int test(int pos) {
                return needle.compareTo(haystack.get(pos));
            }
        };
    }

    /**
     * Convert a possibly-negative binary search result to an index or insertion point.
     *
     * @param res The result
     * @return The index (if `res` is nonnegative) or insertion point (if it is negative).
     */
    public static int resultToIndex(int res) {
        if (res >= 0) {
            return res;
        } else {
            return -res - 1;
        }
    }
}