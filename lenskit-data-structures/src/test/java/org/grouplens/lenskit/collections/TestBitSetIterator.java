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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.util.BitSet;

import org.junit.Test;

import com.google.common.collect.Iterators;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestBitSetIterator {

    @Test
    public void testEmpty() {
        BitSetIterator iter = new BitSetIterator(new BitSet());
        assertFalse(iter.hasNext());
        assertFalse(iter.hasPrevious());
    }

    @Test
    public void testSingle() {
        BitSet s = new BitSet();
        s.set(0);
        BitSetIterator iter = new BitSetIterator(s);
        assertTrue(iter.hasNext());
        assertTrue(iter.hasNext());
        assertThat(iter.next(), equalTo(0));
        assertFalse(iter.hasNext());
        assertFalse(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertTrue(iter.hasPrevious());
        assertThat(iter.previous(), equalTo(0));
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
    }

    @Test
    public void testDouble() {
        BitSet s = new BitSet();
        s.set(0);
        s.set(1);
        BitSetIterator iter = new BitSetIterator(s);

        // do some repeated things to make sure that advancing doesn't break
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertThat(iter.nextInt(), equalTo(0));
        assertTrue(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertThat(iter.previousInt(), equalTo(0));
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertThat(iter.nextInt(), equalTo(0));
        assertThat(iter.nextInt(), equalTo(1));
        assertFalse(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertFalse(iter.hasNext());

        iter = new BitSetIterator(s);
        assertArrayEquals(new Integer[]{0, 1},
                          Iterators.toArray(iter, Integer.class));
    }

    @Test
    public void testSkip() {
        BitSet s = new BitSet();
        s.set(2);
        s.set(5);
        s.set(7);
        BitSetIterator iter = new BitSetIterator(s);

        // do some repeated things to make sure that advancing doesn't break
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertThat(iter.nextInt(), equalTo(2));
        assertTrue(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertThat(iter.previousInt(), equalTo(2));
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertThat(iter.nextInt(), equalTo(2));
        assertThat(iter.nextInt(), equalTo(5));
        assertThat(iter.previousInt(), equalTo(5));
        assertThat(iter.nextInt(), equalTo(5));
        assertThat(iter.nextInt(), equalTo(7));
        assertFalse(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertFalse(iter.hasNext());
        assertThat(iter.previousInt(), equalTo(7));
        assertTrue(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertThat(iter.previousInt(), equalTo(5));
        assertTrue(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertThat(iter.nextInt(), equalTo(5));

        iter = new BitSetIterator(s);
        assertArrayEquals(new Integer[]{2, 5, 7},
                          Iterators.toArray(iter, Integer.class));
    }

    @Test
    public void testStartOffset() {
        BitSet s = new BitSet();
        s.set(2);
        s.set(5);
        BitSetIterator iter = new BitSetIterator(s, 2);
        assertArrayEquals(new Integer[]{2, 5},
                          Iterators.toArray(iter, Integer.class));

        iter = new BitSetIterator(s, 3);
        assertArrayEquals(new Integer[]{5},
                          Iterators.toArray(iter, Integer.class));
    }

    @Test
    public void testStartEndOffset() {
        BitSet s = new BitSet();
        s.set(0);
        s.set(2);
        s.set(5);
        BitSetIterator iter = new BitSetIterator(s, 1, 5);
        assertArrayEquals(new Integer[]{2},
                          Iterators.toArray(iter, Integer.class));

        s.set(4);
        iter = new BitSetIterator(s, 1, 5);
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertThat(iter.nextInt(), equalTo(2));
        assertTrue(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertThat(iter.previousInt(), equalTo(2));
        assertFalse(iter.hasPrevious());
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
        assertThat(iter.nextInt(), equalTo(2));
        assertThat(iter.nextInt(), equalTo(4));
        assertFalse(iter.hasNext());
        assertTrue(iter.hasPrevious());
        assertFalse(iter.hasNext());
        assertThat(iter.previousInt(), equalTo(4));
        assertThat(iter.nextInt(), equalTo(4));
        assertThat(iter.previousInt(), equalTo(4));
        assertThat(iter.previousInt(), equalTo(2));
        assertFalse(iter.hasPrevious());
        assertThat(iter.nextInt(), equalTo(2));
        assertTrue(iter.hasNext());
        assertThat(iter.nextInt(), equalTo(4));
    }
}
