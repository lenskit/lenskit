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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test the array utility methods.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MoreArraysTest {

    @Test
    public void testIsSortedEmpty() {
        assertTrue(MoreArrays.isSorted(new long[]{}, 0, 0));
    }

    @Test
    public void testIsSortedTrue() {
        assertTrue(MoreArrays.isSorted(new long[]{1, 2, 3}, 0, 3));
    }

    @Test
    public void testIsSortedFalse() {
        assertFalse(MoreArrays.isSorted(new long[]{1, 3, 2}, 0, 3));
    }

    @Test
    public void testIsSortedSubset() {
        assertTrue(MoreArrays.isSorted(new long[]{5, 1, 2, 3, -4}, 1, 4));
    }

    @Test
    public void testDeduplicateEmpty() {
        long[] data = {};
        int end = MoreArrays.deduplicate(data, 0, 0);
        assertEquals(0, end);
    }

    @Test
    public void testDeduplicateNoChange() {
        long[] data = {1, 2, 3};
        int end = MoreArrays.deduplicate(data, 0, 3);
        assertEquals(3, end);
        assertThat(data, equalTo(new long[]{1, 2, 3}));
    }

    @Test
    public void testDeduplicateDups() {
        long[] data = {1, 2, 2, 3};
        int end = MoreArrays.deduplicate(data, 0, 4);
        assertEquals(3, end);
        assertEquals(1, data[0]);
        assertEquals(2, data[1]);
        assertEquals(3, data[2]);
    }

    @Test
    public void testDeduplicateDupsEnd() {
        long[] data = {1, 2, 2, 3, 3};
        int end = MoreArrays.deduplicate(data, 0, 5);
        assertEquals(3, end);
        assertEquals(1, data[0]);
        assertEquals(2, data[1]);
        assertEquals(3, data[2]);
    }

    @
            Test
    public void testDeduplicateSubset() {
        long[] data = {1, 1, 2, 2, 3, 3};
        int end = MoreArrays.deduplicate(data, 1, 6);
        assertEquals(4, end);
        assertEquals(1, data[0]);
        assertEquals(1, data[1]);
        assertEquals(2, data[2]);
        assertEquals(3, data[3]);
    }

}
