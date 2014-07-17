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
package org.grouplens.lenskit.collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BitSetIteratorTest {

    @Test
    public void testEmpty() {
        BitSetIterator iter = new BitSetIterator(new BitSet());
        assertFalse(iter.hasNext());
        assertFalse(iter.hasPrevious());
        try {
            iter.previousInt();
            fail("Should throw an exception!");
        } catch(NoSuchElementException e) { /* expected */ }
        
        try {
            iter.nextInt();
            fail("Should throw an exception!");
        } catch(NoSuchElementException e) { /* expected */ }
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
        try {
            iter.previousInt();
            fail("Should throw an exception!");
        } catch(NoSuchElementException e) { /* expected */ }
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
        try {
            iter.nextInt();
            fail("Should throw an exception!");
        } catch(NoSuchElementException e) { /* expected */ }
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
    
    @Test
    public void testStartEndOffset2() {
        BitSet s = new BitSet();
        s.set(5);
        s.set(7);
        s.set(9);
        BitSetIterator iter = new BitSetIterator(s, 6, 8);
        assertArrayEquals(new Integer[]{7},
                          Iterators.toArray(new BitSetIterator(s, 6, 8), Integer.class));
        
        iter = new BitSetIterator(s, 6, 8);
        assertFalse(iter.hasPrevious());

        assertTrue(iter.hasNext());
        iter.nextInt();
        assertFalse(iter.hasNext());
        assertTrue(iter.hasPrevious());
    }

    
    @Test
    public void testFullIteration() {
        List<Integer> inList = Arrays.asList(2, 3, 5, 8, 13, 21, 34); 
        BitSet bset = new BitSet();
        for (int n : inList) bset.set(n);
        List<Integer> outList = Lists.newArrayList(new BitSetIterator(bset));
        assertThat(inList, equalTo(outList));
    }
    
    @Test
    public void testStartEndEmpty() {
        List<Integer> inList = Arrays.asList(2, 3, 5, 8, 13, 21, 34); 
        BitSet bset = new BitSet();
        for (int n : inList) bset.set(n);
        List<Integer> outList = Lists.newArrayList(new BitSetIterator(bset, 9, 13));
        assertThat(new ArrayList<Integer>(outList), equalTo(new ArrayList<Integer>()));
        
        assertFalse(new BitSetIterator(bset, 35, 100).hasPrevious());
        assertFalse(new BitSetIterator(bset, 35, 100).hasNext());
        assertFalse(new BitSetIterator(bset, 0, 100).hasPrevious());
        
        try {
            new BitSetIterator(bset, -1, 100);
            fail("Should throw illegal argument exception");
        } catch(IllegalArgumentException e) { /* expected */ };
        
        try {
            new BitSetIterator(bset, 13, 8);
            fail("Should throw illegal argument exception");
        } catch(IllegalArgumentException e) { /* expected */ };
    }

    @Test
    public void testNonStartInitial() {
        BitSet bset = new BitSet();
        bset.set(1);
        bset.set(2);
        bset.set(4);
        bset.set(5);
        BitSetIterator iter = new BitSetIterator(bset, 0, 6, 2);
        // the iterator should have both next and previous bits
        assertTrue(iter.hasNext());
        assertTrue(iter.hasPrevious());
        // The iterator's 'next' bit should be 2
        assertThat(iter.nextInt(), equalTo(2));
        // followed by 4, of course
        assertThat(iter.nextInt(), equalTo(4));
        // reset, and back should get us 1
        iter = new BitSetIterator(bset, 0, 6, 2);
        assertThat(iter.previousInt(), equalTo(1));
        assertFalse(iter.hasPrevious());
    }

    @Test
    public void testNonStartInitialAtHole() {
        BitSet bset = new BitSet();
        bset.set(1);
        bset.set(2);
        bset.set(4);
        bset.set(5);
        BitSetIterator iter = new BitSetIterator(bset, 0, 6, 3);
        // the iterator should have both next and previous bits
        assertTrue(iter.hasNext());
        assertTrue(iter.hasPrevious());
        // The iterator's 'next' bit should be 4
        assertThat(iter.nextInt(), equalTo(4));
        // followed by 5, of course
        assertThat(iter.nextInt(), equalTo(5));
        assertFalse(iter.hasNext());
        // reset, and back should get us 2
        iter = new BitSetIterator(bset, 0, 6, 3);
        assertThat(iter.previousInt(), equalTo(2));
        assertThat(iter.previousInt(), equalTo(1));
        assertFalse(iter.hasPrevious());
    }

    @Test
    public void testInitiallAtEnd() {
        BitSet bset = new BitSet();
        bset.set(1);
        bset.set(3);
        bset.set(4);
        BitSetIterator iter = new BitSetIterator(bset, 0, 5, 5);
        assertFalse(iter.hasNext());
        assertTrue(iter.hasPrevious());
        try {
            iter.nextInt();
            fail("nextInt should throw");
        } catch (NoSuchElementException e) {
            /* expected */
        }
        // reset, and back should get us 2
        iter = new BitSetIterator(bset, 0, 5, 5);
        assertThat(iter.previousInt(), equalTo(4));
        assertTrue(iter.hasNext());
        assertThat(iter.previousInt(), equalTo(3));
        assertThat(iter.nextInt(), equalTo(3));
        assertThat(iter.previousInt(), equalTo(3));
        assertThat(iter.previousInt(), equalTo(1));
        assertTrue(iter.hasNext());
        assertFalse(iter.hasPrevious());
    }
}
