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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.grouplens.reflens.util.IntSortedArraySet.deduplicate;
import static org.grouplens.reflens.util.IntSortedArraySet.isSorted;
import static org.hamcrest.CoreMatchers.equalTo;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

import java.util.Collections;

import org.junit.Test;


/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestIntSortedArraySet {
	@SuppressWarnings("unchecked")
	private IntSortedArraySet emptySet() {
		return new IntSortedArraySet(Collections.EMPTY_LIST);
	}
	
	@Test
	public void testIsSortedEmpty() {
		assertTrue(isSorted(new int[]{}, 0, 0));
	}
	
	@Test
	public void testIsSortedTrue() {
		assertTrue(isSorted(new int[]{1,2,3}, 0, 3));
	}
	
	@Test
	public void testIsSortedFalse() {
		assertFalse(isSorted(new int[]{1,3,2}, 0, 3));
	}
	
	@Test
	public void testIsSortedSubset() {
		assertTrue(isSorted(new int[]{5,1,2,3,-4}, 1, 4));
	}
	
	@Test
	public void testDeduplicateEmpty() {
		int[] data = {};
		int end = deduplicate(data, 0, 0);
		assertEquals(0, end);
	}
	
	@Test
	public void testDeduplicateNoChange() {
		int[] data = {1, 2, 3};
		int end = deduplicate(data, 0, 3);
		assertEquals(3, end);
		assertThat(data, equalTo(new int[]{1,2,3}));
	}
	
	@Test
	public void testDeduplicateDups() {
		int[] data = {1, 2, 2, 3};
		int end = deduplicate(data, 0, 4);
		assertEquals(3, end);
		assertEquals(1, data[0]);
		assertEquals(2, data[1]);
		assertEquals(3, data[2]);
	}
	
	@Test
	public void testDeduplicateDupsEnd() {
		int[] data = {1, 2, 2, 3, 3};
		int end = deduplicate(data, 0, 5);
		assertEquals(3, end);
		assertEquals(1, data[0]);
		assertEquals(2, data[1]);
		assertEquals(3, data[2]);
	}
	@
	Test
	public void testDeduplicateSubset() {
		int[] data = {1, 1, 2, 2, 3, 3};
		int end = deduplicate(data, 1, 6);
		assertEquals(4, end);
		assertEquals(1, data[0]);
		assertEquals(1, data[1]);
		assertEquals(2, data[2]);
		assertEquals(3, data[3]);
	}
	
	@Test
	public void testEmptySet() {
		IntSortedArraySet set = emptySet();
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
		IntBidirectionalIterator iter = set.iterator();
		assertNotNull(iter);
		assertFalse(iter.hasNext());
		assertFalse(iter.hasPrevious());
		assertFalse(set.contains(42));
		assertFalse(set.contains(Integer.valueOf(42)));
	}
	
	@Test
	public void testEmptySetSubset() {
		IntSortedArraySet set = emptySet();
		IntSortedSet sset = set.headSet(50);
		assertTrue(sset.isEmpty());
		assertEquals(0, sset.size());
		assertFalse(set.iterator().hasNext());
		assertFalse(set.iterator().hasPrevious());
		assertFalse(set.contains(42));
		assertFalse(set.contains(Integer.valueOf(42)));
	}
	
	/**
	 * Run a battery of tests on a standard set. Used to test a variety of
	 * construction scenarios with less code duplication.
	 * @param set The set {2, 5, 6}.
	 */
	private void testSetSimple(IntSortedSet set) {
		assertFalse(set.isEmpty());
		assertEquals(3, set.size());
		assertEquals(2, set.firstInt());
		assertEquals(6, set.lastInt());
		assertEquals(Integer.valueOf(2), set.first());
		assertEquals(Integer.valueOf(6), set.last());
		assertTrue(set.contains(2));
		assertTrue(set.contains(5));
		assertTrue(set.contains(6));
		assertFalse(set.contains(0));
		assertFalse(set.contains(42));
		assertFalse(set.iterator().hasPrevious());
		int[] items = IntIterators.unwrap(set.iterator());
		assertEquals(2, items[0]);
		assertEquals(5, items[1]);
		assertEquals(6, items[2]);
	}
	
	@Test
	public void testArrayCtor() {
		IntSortedSet set = new IntSortedArraySet(new int[]{2, 5, 6});
		testSetSimple(set);
		
		IntBidirectionalIterator iter = set.iterator();
		assertTrue(iter.hasNext());
		assertEquals(2, iter.nextInt());
		assertTrue(iter.hasPrevious());
		assertEquals(2, iter.previousInt());
		assertFalse(iter.hasPrevious());
	}
	@Test
	public void testArrayCtorUnsorted() {
		int[] data = {5, 2, 6};
		IntSortedSet set = new IntSortedArraySet(data);
		testSetSimple(set);
		// make sure it tweaked our array
		assertEquals(2, data[0]);
	}
	@Test
	public void testArrayCtorRanged() {
		IntSortedSet set = new IntSortedArraySet(new int[]{42, 5, 2, 6, 7}, 1, 4);
		testSetSimple(set);
	}
	@Test
	public void testCollectionCtor() {
		int[] data = {5, 2, 6};
		IntSortedSet set = new IntSortedArraySet(new IntArrayList(data));
		testSetSimple(set);
	}
	
	@Test
	public void testHeadSet() {
		int[] data = {7, 5, 2, 6, 42};
		IntSortedSet set = new IntSortedArraySet(data);
		assertEquals(data.length, set.size());
		testSetSimple(set.headSet(7));
	}
	
	@Test
	public void testTailSet() {
		int[] data = {0, 5, 2, 6, 1};
		IntSortedSet set = new IntSortedArraySet(data);
		assertEquals(data.length, set.size());
		testSetSimple(set.tailSet(2));
	}
	
	@Test
	public void testSubSet() {
		int[] data = {0, 42, 5, 2, 6, 1, 7};
		IntSortedSet set = new IntSortedArraySet(data);
		assertEquals(data.length, set.size());
		testSetSimple(set.subSet(2, 7));
	}
	
	@Test
	public void testTailIter() {
		int[] data = {0, 42, 5, 2, 6, 1, 7};
		IntSortedSet set = new IntSortedArraySet(data);
		IntBidirectionalIterator iter = set.iterator(2);
		assertTrue(iter.hasNext());
		assertTrue(iter.hasPrevious());
		assertEquals(5, iter.nextInt());
		iter = set.iterator(2);
		assertEquals(2, iter.previousInt());
		iter = set.iterator(-5);
		assertFalse(iter.hasPrevious());
		iter = set.iterator(100);
		assertFalse(iter.hasNext());
		iter = set.iterator(3);
		assertEquals(5, iter.nextInt());
		iter = set.iterator(3);
		assertEquals(2, iter.previousInt());
	}
	
	@Test
	public void testRemoveDuplicates() {
		int[] data = {5, 2, 6, 2};
		IntSortedSet set = new IntSortedArraySet(data);
		assertEquals(2, data[0]);
		testSetSimple(set);
	}
}
