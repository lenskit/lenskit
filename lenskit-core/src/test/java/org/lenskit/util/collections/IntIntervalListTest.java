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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class IntIntervalListTest {

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
        IntList list = new IntIntervalList(42, 43);
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

    @Test
    public void testBroaderInterval() {
        IntList list = new IntIntervalList(5);
        assertFalse(list.isEmpty());
        assertEquals(5, list.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(i, list.getInt(i));
        }
        try {
            list.getInt(5);
            fail("getInt(5) should throw");
        } catch (IndexOutOfBoundsException e) {
            /* no-op */
        }
    }
}
