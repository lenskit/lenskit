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

/**
 * Extra array utilities.
 */
public final class MoreArrays {
    private MoreArrays() {
    }

    /**
     * Check that the array is sorted. Duplicates are not allowed in a sorted array, by this
     * method's definition.
     *
     * @param data  The data to test for sortedness.
     * @param start The beginning of the range to test (inclusive)
     * @param end   The end of the range to test (exclusive).
     * @return {@code true} iff the array is sorted.
     */
    public static boolean isSorted(final long[] data, final int start, final int end) {
        for (int i = start; i < end - 1; i++) {
            if (data[i] >= data[i + 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove duplicate elements in the backing store. The array should be
     * sorted.
     *
     * @param data  The data to deduplicate.
     * @param start The beginning of the range to deduplicate (inclusive).
     * @param end   The end of the range to deduplicate (exclusive).
     * @return the new end index of the array
     */
    public static int deduplicate(final long[] data, final int start, final int end) {
        if (start == end) {
            return end;   // special-case empty arrays
        }

        // Since we have a non-empty array, the nextPos will always be where the
        // end is if we find no more unique elements.
        int pos = start + 1;
        for (int i = pos; i < end; i++) {
            if (data[i] != data[i - 1]) { // we have a non-duplicate item
                if (i != pos) {           // indices out of alignment, must copy
                    data[pos] = data[i];
                }
                pos++;                  // increment nextPos since we have a new non-dup
            }
            // if data[i] is a duplicate, then i steps forward and nextPos doesn't,
            // thereby arranging for data[i] to be elided.
        }
        return pos;
    }

    /**
     * Remove duplicate elements in the backing store. The array should be
     * sorted.
     *
     * @param data  The data to deduplicate.
     * @param start The beginning of the range to deduplicate (inclusive).
     * @param end   The end of the range to deduplicate (exclusive).
     * @return the new end index of the array
     */
    public static int deduplicate(final int[] data, final int start, final int end) {
        if (start == end) {
            return end;   // special-case empty arrays
        }

        // Since we have a non-empty array, the nextPos will always be where the
        // end is if we find no more unique elements.
        int pos = start + 1;
        for (int i = pos; i < end; i++) {
            if (data[i] != data[i - 1]) { // we have a non-duplicate item
                if (i != pos) {           // indices out of alignment, must copy
                    data[pos] = data[i];
                }
                pos++;                  // increment nextPos since we have a new non-dup
            }
            // if data[i] is a duplicate, then i steps forward and nextPos doesn't,
            // thereby arranging for data[i] to be elided.
        }
        return pos;
    }
}
