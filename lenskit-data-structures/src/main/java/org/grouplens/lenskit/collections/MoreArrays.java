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

/**
 * Additional array utilities.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
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
