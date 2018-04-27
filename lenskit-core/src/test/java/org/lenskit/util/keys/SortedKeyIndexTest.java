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

import it.unimi.dsi.fastutil.longs.LongLists;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test long key sets.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SortedKeyIndexTest {
    @Test
    public void testEmptyArray() {
        long[] rawKeys = {};
        SortedKeyIndex keys = SortedKeyIndex.wrap(rawKeys, 0);
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.keySet(), hasSize(0));
        assertThat(keys.getKeyList(), hasSize(0));
        assertThat(keys.tryGetIndex(42), lessThan(0));
    }

    @Test
    public void testEmptyCollection() {
        SortedKeyIndex keys = SortedKeyIndex.fromCollection(LongLists.EMPTY_LIST);
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.keySet(), hasSize(0));
        assertThat(keys.getKeyList(), hasSize(0));
    }

    @Test
    public void testEmptyUpperBound() {
        SortedKeyIndex keys = SortedKeyIndex.empty();
        assertThat(keys.findUpperBound(0), equalTo(0));
    }

    @Test
    public void testSingletonUpperBound() {
        SortedKeyIndex keys = SortedKeyIndex.create(5);
        assertThat(keys.findUpperBound(0), equalTo(0));
        assertThat(keys.findUpperBound(5), equalTo(1));
        assertThat(keys.findUpperBound(7), equalTo(1));
    }

    @Test
    public void testSomeKeysUpperBound() {
        SortedKeyIndex keys = SortedKeyIndex.create(5, 6, 8);
        assertThat(keys.findUpperBound(0), equalTo(0));
        assertThat(keys.findUpperBound(5), equalTo(1));
        assertThat(keys.findUpperBound(6), equalTo(2));
        assertThat(keys.findUpperBound(7), equalTo(2));
        assertThat(keys.findUpperBound(8), equalTo(3));
        assertThat(keys.findUpperBound(10), equalTo(3));
    }

    @Test
    public void testEmptyLowerBound() {
        SortedKeyIndex keys = SortedKeyIndex.empty();
        assertThat(keys.findLowerBound(0), equalTo(0));
    }

    @Test
    public void testSingletonLowerBound() {
        SortedKeyIndex keys = SortedKeyIndex.create(5);
        assertThat(keys.findLowerBound(0), equalTo(0));
        assertThat(keys.findLowerBound(5), equalTo(0));
        assertThat(keys.findLowerBound(7), equalTo(1));
    }

    @Test
    public void testSomeKeysLowerBound() {
        SortedKeyIndex keys = SortedKeyIndex.create(5, 6, 8);
        assertThat(keys.findLowerBound(0), equalTo(0));
        assertThat(keys.findLowerBound(5), equalTo(0));
        assertThat(keys.findLowerBound(6), equalTo(1));
        assertThat(keys.findLowerBound(7), equalTo(2));
        assertThat(keys.findLowerBound(8), equalTo(2));
        assertThat(keys.findLowerBound(10), equalTo(3));
    }

    @Test
    public void testSubViewLowerBound() {
        SortedKeyIndex keys = SortedKeyIndex.create(0, 1, 2, 3, 4, 5, 6, 7);
        SortedKeyIndex subk = keys.subIndex(1, 5);
        assertThat(subk.getLowerBound(), equalTo(1));
        assertThat(subk.getUpperBound(), equalTo(5));
        assertThat(subk.tryGetIndex(1), equalTo(1));
        assertThat(subk.tryGetIndex(4), equalTo(4));
        assertThat(subk.tryGetIndex(5), lessThan(0));
        assertThat(subk.tryGetIndex(6), lessThan(0));
        assertThat(subk.tryGetIndex(0), lessThan(0));
        assertThat(subk.findLowerBound(1L), equalTo(1));
        assertThat(subk.findLowerBound(0L), equalTo(1));
        assertThat(subk.findLowerBound(2L), equalTo(2));
        assertThat(subk.findLowerBound(7L), equalTo(5));
        assertThat(subk.findUpperBound(1), equalTo(2));
        assertThat(subk.findUpperBound(5), equalTo(5));
        assertThat(subk.findUpperBound(4), equalTo(5));
    }
}
