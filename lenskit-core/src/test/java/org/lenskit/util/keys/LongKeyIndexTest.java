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
package org.lenskit.util.keys;

import it.unimi.dsi.fastutil.longs.LongLists;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test long key sets.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LongKeyIndexTest {
    @Test
    public void testEmptyArray() {
        long[] rawKeys = {};
        LongKeyIndex keys = LongKeyIndex.wrap(rawKeys, 0);
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.keySet(), hasSize(0));
        assertThat(keys.keyList(), hasSize(0));
        assertThat(keys.getIndex(42), lessThan(0));
    }

    @Test
    public void testEmptyCollection() {
        LongKeyIndex keys = LongKeyIndex.fromCollection(LongLists.EMPTY_LIST);
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.keySet(), hasSize(0));
        assertThat(keys.keyList(), hasSize(0));
    }

    @Test
    public void testEmptyUpperBound() {
        LongKeyIndex keys = LongKeyIndex.empty();
        assertThat(keys.findUpperBound(0), equalTo(0));
    }

    @Test
    public void testSingletonUpperBound() {
        LongKeyIndex keys = LongKeyIndex.create(5);
        assertThat(keys.findUpperBound(0), equalTo(0));
        assertThat(keys.findUpperBound(5), equalTo(1));
        assertThat(keys.findUpperBound(7), equalTo(1));
    }

    @Test
    public void testSomeKeysUpperBound() {
        LongKeyIndex keys = LongKeyIndex.create(5, 6, 8);
        assertThat(keys.findUpperBound(0), equalTo(0));
        assertThat(keys.findUpperBound(5), equalTo(1));
        assertThat(keys.findUpperBound(6), equalTo(2));
        assertThat(keys.findUpperBound(7), equalTo(2));
        assertThat(keys.findUpperBound(8), equalTo(3));
        assertThat(keys.findUpperBound(10), equalTo(3));
    }

    @Test
    public void testEmptyLowerBound() {
        LongKeyIndex keys = LongKeyIndex.empty();
        assertThat(keys.findLowerBound(0), equalTo(0));
    }

    @Test
    public void testSingletonLowerBound() {
        LongKeyIndex keys = LongKeyIndex.create(5);
        assertThat(keys.findLowerBound(0), equalTo(0));
        assertThat(keys.findLowerBound(5), equalTo(0));
        assertThat(keys.findLowerBound(7), equalTo(1));
    }

    @Test
    public void testSomeKeysLowerBound() {
        LongKeyIndex keys = LongKeyIndex.create(5, 6, 8);
        assertThat(keys.findLowerBound(0), equalTo(0));
        assertThat(keys.findLowerBound(5), equalTo(0));
        assertThat(keys.findLowerBound(6), equalTo(1));
        assertThat(keys.findLowerBound(7), equalTo(2));
        assertThat(keys.findLowerBound(8), equalTo(2));
        assertThat(keys.findLowerBound(10), equalTo(3));
    }

    @Test
    public void testSubViewLowerBound() {
        LongKeyIndex keys = LongKeyIndex.create(0,1,2,3,4,5,6,7);
        LongKeyIndex subk = keys.subIndex(1, 5);
        assertThat(subk.getLowerBound(), equalTo(1));
        assertThat(subk.getUpperBound(), equalTo(5));
        assertThat(subk.getIndex(1), equalTo(1));
        assertThat(subk.getIndex(4), equalTo(4));
        assertThat(subk.getIndex(5), lessThan(0));
        assertThat(subk.getIndex(6), lessThan(0));
        assertThat(subk.getIndex(0), lessThan(0));
        assertThat(subk.findLowerBound(1L), equalTo(1));
        assertThat(subk.findLowerBound(0L), equalTo(1));
        assertThat(subk.findLowerBound(2L), equalTo(2));
        assertThat(subk.findLowerBound(7L), equalTo(5));
        assertThat(subk.findUpperBound(1), equalTo(2));
        assertThat(subk.findUpperBound(5), equalTo(5));
        assertThat(subk.findUpperBound(4), equalTo(5));
    }
}
