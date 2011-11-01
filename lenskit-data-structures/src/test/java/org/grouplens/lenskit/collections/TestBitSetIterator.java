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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.array;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

import org.junit.Test;

import com.google.common.collect.Iterators;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
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
    
    @SuppressWarnings("unchecked")
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
        assertThat(Iterators.toArray(iter, Integer.class),
                   array(equalTo(0), equalTo(1)));
    }
    
    @SuppressWarnings("unchecked")
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
        assertThat(Iterators.toArray(iter, Integer.class),
                   array(equalTo(2), equalTo(5), equalTo(7)));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testStartOffset() {
        BitSet s = new BitSet();
        s.set(2);
        s.set(5);
        BitSetIterator iter = new BitSetIterator(s, 2);
        assertThat(Iterators.toArray(iter, Integer.class),
                   array(equalTo(2), equalTo(5)));
        
        iter = new BitSetIterator(s, 3);
        assertThat(Iterators.toArray(iter, Integer.class),
                   array(equalTo(5)));        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testStartEndOffset() {
        BitSet s = new BitSet();
        s.set(0);
        s.set(2);
        s.set(5);
        BitSetIterator iter = new BitSetIterator(s, 1, 5);
        assertThat(Iterators.toArray(iter, Integer.class),
                   array(equalTo(2)));
        
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
