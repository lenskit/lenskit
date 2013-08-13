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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;


/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestLongSortedArraySet {
    @SuppressWarnings("unchecked")
    private LongSortedArraySet emptySet() {
        return new LongSortedArraySet(Collections.EMPTY_LIST);
    }

    @Test
    public void testEmptySet() {
        LongSortedArraySet set = emptySet();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        LongBidirectionalIterator iter = set.iterator();
        assertNotNull(iter);
        assertFalse(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertFalse(set.contains(42));
        assertFalse(set.contains(Long.valueOf(42)));
    }

    @Test
    public void testEmptySetSubset() {
        LongSortedArraySet set = emptySet();
        LongSortedSet sset = set.headSet(50);
        assertTrue(sset.isEmpty());
        assertEquals(0, sset.size());
        assertFalse(set.iterator().hasNext());
        assertFalse(set.iterator().hasPrevious());
        assertFalse(set.contains(42));
        assertFalse(set.contains(Long.valueOf(42)));
    }

    /**
     * Run a battery of tests on a standard set. Used to test a variety of
     * construction scenarios with less code duplication.
     *
     * @param set The set {2, 5, 6}.
     */
    private void testSetSimple(LongSortedSet set) {
        assertFalse(set.isEmpty());
        assertEquals(3, set.size());
        assertEquals(2, set.firstLong());
        assertEquals(6, set.lastLong());
        assertEquals(Long.valueOf(2), set.first());
        assertEquals(Long.valueOf(6), set.last());
        assertTrue(set.contains(2));
        assertTrue(set.contains(5));
        assertTrue(set.contains(6));
        assertFalse(set.contains(0));
        assertFalse(set.contains(42));
        assertFalse(set.iterator().hasPrevious());
        long[] items = LongIterators.unwrap(set.iterator());
        assertEquals(2, items[0]);
        assertEquals(5, items[1]);
        assertEquals(6, items[2]);
    }

    @Test
    public void testArrayCtor() {
        LongSortedSet set = new LongSortedArraySet(new long[]{2, 5, 6});
        testSetSimple(set);

        LongBidirectionalIterator iter = set.iterator();
        assertTrue(iter.hasNext());
        assertEquals(2, iter.nextLong());
        assertTrue(iter.hasPrevious());
        assertEquals(2, iter.previousLong());
        assertFalse(iter.hasPrevious());
    }

    @Test
    public void testArrayCtorUnsorted() {
        long[] data = {5, 2, 6};
        LongSortedSet set = new LongSortedArraySet(data);
        testSetSimple(set);
    }

    @Test
    public void testCollectionCtor() {
        long[] data = {5, 2, 6};
        LongSortedSet set = new LongSortedArraySet(new LongArrayList(data));
        testSetSimple(set);
    }

    @Test
    public void testHeadSet() {
        long[] data = {7, 5, 2, 6, 42};
        LongSortedSet set = new LongSortedArraySet(data);
        assertEquals(data.length, set.size());
        testSetSimple(set.headSet(7));
    }

    @Test
    public void testTailSet() {
        long[] data = {0, 5, 2, 6, 1};
        LongSortedSet set = new LongSortedArraySet(data);
        assertEquals(data.length, set.size());
        testSetSimple(set.tailSet(2));
    }

    @Test
    public void testSubSet() {
        long[] data = {0, 42, 5, 2, 6, 1, 7};
        LongSortedSet set = new LongSortedArraySet(data);
        assertEquals(data.length, set.size());
        testSetSimple(set.subSet(2, 7));
    }

    @Test
    public void testTailIter() {
        long[] data = {0, 42, 5, 2, 6, 1, 7};
        LongSortedSet set = new LongSortedArraySet(data);
        LongBidirectionalIterator iter = set.iterator(2);
        assertTrue(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertEquals(5, iter.nextLong());
        iter = set.iterator(2);
        assertEquals(2, iter.previousLong());
        iter = set.iterator(-5);
        assertFalse(iter.hasPrevious());
        iter = set.iterator(100);
        assertFalse(iter.hasNext());
        iter = set.iterator(3);
        assertEquals(5, iter.nextLong());
        iter = set.iterator(3);
        assertEquals(2, iter.previousLong());
    }

    @Test
    public void testRemoveDuplicates() {
        long[] data = {5, 2, 6, 2};
        LongSortedSet set = new LongSortedArraySet(data);
        testSetSimple(set);
    }

    @Test
    public void testMaskFirst() {
        LongKeySet keys = LongKeySet.create(2, 7, 8, 42, 639);
        keys.setActive(0, false);
        LongSortedSet set = new LongSortedArraySet(keys);
        assertThat(set, hasSize(4));
        assertThat(set.first(), equalTo(7l));
        assertThat(set.last(), equalTo(639l));
        assertTrue(set.contains(7));
        assertTrue(set.contains(42));
        assertFalse(set.contains(2));
        assertThat(LongIterators.unwrap(set.iterator()),
                   equalTo(new long[]{7, 8, 42, 639}));
        assertThat(LongIterators.unwrap(set.iterator(2)),
                   equalTo(new long[]{7, 8, 42, 639}));
        assertThat(LongIterators.unwrap(set.iterator(7)),
                   equalTo(new long[]{8, 42, 639}));
        assertThat(set.headSet(42), hasSize(2));
    }

    @Test
    public void testMaskMid() {
        LongKeySet keys = LongKeySet.create(2, 7, 8, 42, 639);
        keys.setActive(2, false);
        LongSortedSet set = new LongSortedArraySet(keys);
        assertThat(set, hasSize(4));
        assertThat(set.first(), equalTo(2l));
        assertThat(set.last(), equalTo(639l));
        assertTrue(set.contains(7));
        assertTrue(set.contains(42));
        assertFalse(set.contains(8));
        assertThat(LongIterators.unwrap(set.iterator()),
                   equalTo(new long[]{2, 7, 42, 639}));
        assertThat(LongIterators.unwrap(set.iterator(2)),
                   equalTo(new long[]{7, 42, 639}));
        assertThat(LongIterators.unwrap(set.iterator(7)),
                   equalTo(new long[]{42, 639}));
        assertThat(set.headSet(42), hasSize(2));
        assertThat(set.toLongArray(),
                   equalTo(new long[]{2, 7, 42, 639}));
    }

    @Test
    public void testMaskLast() {
        LongKeySet keys = LongKeySet.create(2, 7, 8, 42, 639);
        keys.setActive(4, false);
        LongSortedSet set = new LongSortedArraySet(keys);
        assertThat(set, hasSize(4));
        assertThat(set.first(), equalTo(2l));
        assertThat(set.last(), equalTo(42l));
        assertTrue(set.contains(8));
        assertFalse(set.contains(639));
        assertThat(LongIterators.unwrap(set.iterator()),
                   equalTo(new long[]{2, 7, 8, 42}));
        assertThat(LongIterators.unwrap(set.iterator(2)),
                   equalTo(new long[]{7, 8, 42}));
        assertThat(LongIterators.unwrap(set.iterator(7)),
                   equalTo(new long[]{8, 42}));
        assertThat(set.headSet(42).toLongArray(),
                   equalTo(new long[]{2, 7, 8}));
        assertThat(set.tailSet(7).toLongArray(),
                   equalTo(new long[]{7, 8, 42}));
    }

    @Test
    public void testMaskEmpty() {
        LongKeySet keys = LongKeySet.create(2, 7, 8, 42, 639);
        keys.setAllActive(false);
        LongSortedSet set = new LongSortedArraySet(keys);

        assertThat(set.size(), equalTo(0));
        assertTrue(set.isEmpty());
        assertFalse(set.iterator().hasNext());
        for (int i = 0; i < keys.domainSize(); i++) {
            assertFalse(set.contains(keys.getKey(i)));
        }
    }

    @Test
    public void testMaskedIterator() {
        LongKeySet keys = LongKeySet.create(2, 7, 8, 42, 639);
        keys.setAllActive(true);
        keys.setActive(0, false);
        keys.setActive(4, false);
        LongSortedSet set = new LongSortedArraySet(keys);
        assertTrue(set.iterator(7).hasNext());
        assertTrue(set.iterator(7).hasPrevious());
        assertThat(set.iterator(7).nextLong(), equalTo(8l));
        assertThat(set.iterator(7).previousLong(), equalTo(7l));
    }

    @Test
    public void testMaskedIteratorMid() {
        LongKeySet keys = LongKeySet.create(2, 7, 8, 42, 639);
        keys.setAllActive(true);
        keys.setActive(0, false);
        keys.setActive(2, false);
        LongSortedSet set = new LongSortedArraySet(keys);
        assertTrue(set.iterator(8).hasNext());
        assertTrue(set.iterator(8).hasPrevious());
        assertThat(set.iterator(8).nextLong(), equalTo(42l));
        assertThat(set.iterator(8).previousLong(), equalTo(7l));
    }
}
