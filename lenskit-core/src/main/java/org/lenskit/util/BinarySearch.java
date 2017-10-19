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

        while (end > start) {
            int off = (end - start) / 2;
            int pos = start + off;
            int cmp = test(pos);
            if (cmp < 0) {
                // target less than pos, go left
                end = pos;
            } else if (cmp > 0) {
                // target more than pos, go right
                start = pos + 1;
            } else {
                // we found it - walk backwards to first
                while (pos > start && test(pos-1) == 0) {
                    pos -= 1;
                }
                return pos;
            }
        }

        // never found it
        return -end - 1;
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