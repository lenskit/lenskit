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
package org.grouplens.lenskit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;

import org.junit.Test;


/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestIntIntervalList {

	@Test
	public void testEmptyList() {
		IntList list = new IntIntervalList(0);
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
		assertFalse(list.iterator().hasNext());
	}
	
	@Test
	public void testEmptyRange() {
		IntList list = new IntIntervalList(5, 5);
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
		assertFalse(list.iterator().hasNext());
	}
	
	@Test
	public void testSimpleListAccess() {
		IntList list = new IntIntervalList(1);
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
		assertEquals(0, list.getInt(0));
		try {
			list.getInt(1);
			fail("getInt(1) should throw");
		} catch (IndexOutOfBoundsException e) {
			/* no-op */
		}
		IntListIterator iter = list.iterator();
		assertTrue(iter.hasNext());
		assertFalse(iter.hasPrevious());
		assertEquals(0, iter.nextInt());
		assertFalse(iter.hasNext());
		assertTrue(iter.hasPrevious());
		assertEquals(0, iter.previousInt());
	}
	
	@Test
    public void testSimpleIntervalAccess() {
        IntList list = new IntIntervalList(42,43);
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(42, list.getInt(0));
        try {
            list.getInt(1);
            fail("getInt(1) should throw");
        } catch (IndexOutOfBoundsException e) {
            /* no-op */
        }
        IntListIterator iter = list.iterator();
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertEquals(42, iter.nextInt());
        assertFalse(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertEquals(42, iter.previousInt());
    }
}
