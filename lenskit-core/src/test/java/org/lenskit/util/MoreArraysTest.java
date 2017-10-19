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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

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
