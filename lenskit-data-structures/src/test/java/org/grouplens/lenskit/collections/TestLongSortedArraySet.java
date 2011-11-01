/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import static org.grouplens.lenskit.collections.LongSortedArraySet.deduplicate;
import static org.grouplens.lenskit.collections.LongSortedArraySet.isSorted;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.BitSet;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;


/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestLongSortedArraySet {
    @SuppressWarnings("unchecked")
    private LongSortedArraySet emptySet() {
        return new LongSortedArraySet(Collections.EMPTY_LIST);
    }

    @Test
    public void testIsSortedEmpty() {
        assertTrue(isSorted(new long[]{}, 0, 0));
    }

    @Test
    public void testIsSortedTrue() {
        assertTrue(isSorted(new long[]{1,2,3}, 0, 3));
    }

    @Test
    public void testIsSortedFalse() {
        assertFalse(isSorted(new long[]{1,3,2}, 0, 3));
    }

    @Test
    public void testIsSortedSubset() {
        assertTrue(isSorted(new long[]{5,1,2,3,-4}, 1, 4));
    }

    @Test
    public void testDeduplicateEmpty() {
        long[] data = {};
        int end = deduplicate(data, 0, 0);
        assertEquals(0, end);
    }

    @Test
    public void testDeduplicateNoChange() {
        long[] data = {1, 2, 3};
        int end = deduplicate(data, 0, 3);
        assertEquals(3, end);
        assertThat(data, equalTo(new long[]{1,2,3}));
    }

    @Test
    public void testDeduplicateDups() {
        long[] data = {1, 2, 2, 3};
        int end = deduplicate(data, 0, 4);
        assertEquals(3, end);
        assertEquals(1, data[0]);
        assertEquals(2, data[1]);
        assertEquals(3, data[2]);
    }

    @Test
    public void testDeduplicateDupsEnd() {
        long[] data = {1, 2, 2, 3, 3};
        int end = deduplicate(data, 0, 5);
        assertEquals(3, end);
        assertEquals(1, data[0]);
        assertEquals(2, data[1]);
        assertEquals(3, data[2]);
    }
    @
    Test
    public void testDeduplicateSubset() {
        long[] data = {1, 1, 2, 2, 3, 3};
        int end = deduplicate(data, 1, 6);
        assertEquals(4, end);
        assertEquals(1, data[0]);
        assertEquals(1, data[1]);
        assertEquals(2, data[2]);
        assertEquals(3, data[3]);
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
        // make sure it tweaked our array
        assertEquals(2, data[0]);
    }
    @Test
    public void testArrayCtorRanged() {
        LongSortedSet set = new LongSortedArraySet(new long[]{42, 5, 2, 6, 7}, 1, 4);
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
        assertEquals(2, data[0]);
        testSetSimple(set);
    }
    
    @Test
    public void testMaskFirst() {
        long[] data = {2, 7, 8, 42, 639};
        BitSet bits = new BitSet(data.length);
        bits.set(1, data.length);
        LongSortedSet set = LongSortedArraySet.wrap(data, data.length, bits);
        assertThat(set.size(), equalTo(4));
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
        long[] data = {2, 7, 8, 42, 639};
        BitSet bits = new BitSet(data.length);
        bits.set(0, data.length);
        bits.clear(2);
        LongSortedSet set = LongSortedArraySet.wrap(data, data.length, bits);
        assertThat(set.size(), equalTo(4));
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
        long[] data = {2, 7, 8, 42, 639};
        BitSet bits = new BitSet(data.length);
        bits.set(0, data.length - 1);
        LongSortedSet set = LongSortedArraySet.wrap(data, data.length, bits);
        assertThat(set.size(), equalTo(4));
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
        long[] data = {2, 7, 8, 42, 639};
        BitSet bits = new BitSet(data.length);
        LongSortedSet set = LongSortedArraySet.wrap(data, data.length, bits);
        
        assertThat(set.size(), equalTo(0));
        assertTrue(set.isEmpty());
        assertFalse(set.iterator().hasNext());
        for (int i = 0; i < data.length; i++) {
            assertFalse(set.contains(data[i]));
        }
    }
    
    @Test @Ignore
    public void testMaskedIterator() {
        long[] data = {2, 7, 8, 42, 639};
        BitSet bits = new BitSet(data.length);
        bits.set(1, data.length - 1);
        LongSortedSet set = LongSortedArraySet.wrap(data, data.length, bits);
        assertTrue(set.iterator(7).hasNext());
        assertTrue(set.iterator(7).hasPrevious());
        assertThat(set.iterator(7).nextLong(), equalTo(8l));
        assertThat(set.iterator(7).previousLong(), equalTo(7l));
    }
    
    @Test @Ignore
    public void testMaskedIteratorMid() {
        long[] data = {2, 7, 8, 42, 639};
        BitSet bits = new BitSet(data.length);
        bits.set(1, data.length - 1);
        bits.clear(2);
        LongSortedSet set = LongSortedArraySet.wrap(data, data.length, bits);
        assertTrue(set.iterator(8).hasNext());
        assertTrue(set.iterator(8).hasPrevious());
        assertThat(set.iterator(8).nextLong(), equalTo(42l));
        assertThat(set.iterator(8).previousLong(), equalTo(7l));
    }
    
    
}
