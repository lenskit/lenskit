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
