/*
 * Copyright 2011 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.grouplens.reflens.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collections;

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
}
