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

import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import static org.grouplens.common.test.MoreMatchers.notANumber;

import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static java.lang.Double.isNaN;
import static java.lang.Math.E;
import static java.lang.Math.PI;
import static org.junit.Assert.*;

/**
 * Tests for {@link ScoredLongArrayList}.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class TestScoredLongArrayList {
    @Test
    public void testEmpty() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        assertTrue(l.isEmpty());
        assertEquals(0, l.size());

        l = new ScoredLongArrayList(50);
        assertTrue(l.isEmpty());
        assertEquals(0, l.size());
    }

    @Test
    public void emptyEquals() {
        ScoredLongList l1 = new ScoredLongArrayList();
        ScoredLongList l2 = new ScoredLongArrayList();
        assertEquals(l1, l1);
        assertEquals(l1, l2);
    }

    @Test
    public void simpleEquals() {
        ScoredLongList le = new ScoredLongArrayList();
        ScoredLongList l1 = new ScoredLongArrayList();
        ScoredLongList l2 = new ScoredLongArrayList();

        l1.add(1);
        l2.add(1);

        assertFalse(le.equals(l1));
        assertTrue(l1.equals(l2));
    }

    @Test
    public void testAdd() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        assertTrue(l.add(5, Math.PI));
        assertFalse(l.isEmpty());
        assertEquals(1, l.size());
        assertEquals(5, l.getLong(0));
        assertEquals(5, l.get(0).longValue());
        assertEquals(Math.PI, l.getScore(0), 0.00001);
    }

    @Test
    public void testAddEmptyIndex() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        l.add(0, 5, Math.PI);
        assertFalse(l.isEmpty());
        assertEquals(1, l.size());
        assertEquals(5, l.getLong(0));
        assertEquals(5, l.get(0).longValue());
        assertEquals(Math.PI, l.getScore(0), 0.00001);
    }

    @Test
    public void testAddNoScore() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        assertTrue(l.add(5));
        assertFalse(l.isEmpty());
        assertEquals(1, l.size());
        assertEquals(5, l.getLong(0));
        assertEquals(5, l.get(0).longValue());
        assertThat(l.getScore(0), notANumber());
    }

    @Test
    public void testAddEmptyIndexNoScore() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        l.add(0, 5);
        assertFalse(l.isEmpty());
        assertEquals(1, l.size());
        assertEquals(5, l.getLong(0));
        assertEquals(5, l.get(0).longValue());
        assertThat(l.getScore(0), notANumber());
    }

    @Test
    public void testAddShift() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        l.add(5, Math.PI);
        l.add(10, Math.E);
        l.add(1, 3, 7.5);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        assertEquals(5, l.getLong(0));
        assertEquals(3, l.getLong(1));
        assertEquals(10, l.getLong(2));
        assertEquals(Math.PI, l.getScore(0), 1.0e-5);
        assertEquals(7.5, l.getScore(1), 1.0e-5);
        assertEquals(Math.E, l.getScore(2), 1.0e-5);
    }

    @Test
    public void testAddShiftNoScore() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        l.add(5, Math.PI);
        l.add(10, Math.E);
        l.add(1, 3);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        assertEquals(5, l.getLong(0));
        assertEquals(3, l.getLong(1));
        assertEquals(10, l.getLong(2));
        assertEquals(Math.PI, l.getScore(0), 1.0e-5);
        assertThat(l.getScore(1), notANumber());
        assertEquals(Math.E, l.getScore(2), 1.0e-5);
    }

    @Test
    public void testAddAtEnd() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        l.add(5, Math.PI);
        l.add(10, Math.E);
        l.add(2, 3, 7.5);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        assertEquals(5, l.getLong(0));
        assertEquals(3, l.getLong(2));
        assertEquals(10, l.getLong(1));
        assertEquals(Math.PI, l.getScore(0), 1.0e-5);
        assertEquals(7.5, l.getScore(2), 1.0e-5);
        assertEquals(Math.E, l.getScore(1), 1.0e-5);
    }

    @Test
    public void testUnscoredArrayConstruct() {
        long[] items = {1, 2, 5};
        ScoredLongArrayList l = new ScoredLongArrayList(items);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(items[i], l.getLong(i));
        }
        for (int i = 0; i < 3; i++) {
            assertThat(l.getScore(i), notANumber());
        }
    }

    @Test
    public void testScoredArrayConstruct() {
        long[] items = {1, 2, 5};
        double[] scores = {1.5, 0.3, 7.9};
        ScoredLongArrayList l = new ScoredLongArrayList(items, scores);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(items[i], l.getLong(i));
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(scores[i], l.getScore(i), 1.0e-5);
        }
        // verify that the arrays are disconnected
        items[1] = 72;
        assertEquals(2, l.getLong(1));
    }

    @Test
    public void testGetElements() {
        long[] items = {1, 2, 5};
        double[] scores = {1.5, 0.3, 7.9};
        ScoredLongArrayList l = new ScoredLongArrayList(items, scores);
        long[] iout = new long[3];
        l.getElements(0, iout, 0, 3);
        assertArrayEquals(items, iout);

        // test with offsets
        iout = new long[5];
        Arrays.fill(iout, 42);
        l.getElements(0, iout, 1, 3);
        assertEquals(42, iout[0]);
        assertEquals(42, iout[4]);
        for (int i = 0; i < 3; i++) {
            assertEquals(items[i], iout[i + 1]);
        }
    }

    @Test
    public void testGetElementsScored() {
        long[] items = {1, 2, 5};
        double[] scores = {1.5, 0.3, 7.9};
        ScoredLongArrayList l = new ScoredLongArrayList(items, scores);
        long[] iout = new long[3];
        double[] sout = new double[3];
        l.getElements(0, iout, sout, 0, 3);
        assertArrayEquals(items, iout);
        assertArrayEquals(scores, sout, 1.0e-5);

        // test with offsets
        iout = new long[5];
        sout = new double[5];
        Arrays.fill(iout, 42);
        l.getElements(0, iout, sout, 1, 3);
        assertEquals(42, iout[0]);
        assertEquals(42, iout[4]);
        for (int i = 0; i < 3; i++) {
            assertEquals(items[i], iout[i + 1]);
            assertEquals(scores[i], sout[i + 1], 1.0e-5);
        }
    }

    @Test
    public void addElements() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        long[] items = {1, 2, 5};
        l.addElements(0, items);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        for (int i = 0; i < items.length; i++) {
            assertEquals(items[i], l.getLong(i));
            assertThat(l.getScore(i), notANumber());
        }
    }

    @Test
    public void addElementsScored() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        long[] items = {1, 2, 5};
        double[] scores = {PI, E, 42};
        l.addElements(0, items, scores);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        for (int i = 0; i < items.length; i++) {
            assertEquals(items[i], l.getLong(i));
            assertEquals(scores[i], l.getScore(i), 1.0e-5);
        }
    }

    @Test
    public void addUnscoredElementsOffset() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        long[] items = {42, 1, 2, 5, 37};
        l.addElements(0, items, 1, 3);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(items[i + 1], l.getLong(i));
            assertThat(l.getScore(i), notANumber());
        }
    }

    @Test
    public void addElementsOffset() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        long[] items = {42, 1, 2, 5, 37};
        double[] scores = {-1, PI, E, 42, -7};
        l.addElements(0, items, scores, 1, 3);
        assertFalse(l.isEmpty());
        assertEquals(3, l.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(items[i + 1], l.getLong(i));
            assertEquals(scores[i + 1], l.getScore(i), 1.0e-5);
        }
    }

    @Test
    public void addElementsShift() {
        long[] items = {42, 1, 2, 5, 37};
        double[] scores = {-1, PI, E, 42, -7};
        ScoredLongList l = new ScoredLongArrayList(items, scores);
        long[] ni = {8, 4, 6};
        l.addElements(2, ni);
        assertEquals(8, l.size());

        long[] ois = new long[5];
        double[] oss = new double[5];
        l.getElements(0, ois, oss, 0, 2);
        l.getElements(5, ois, oss, 2, 3);
        assertArrayEquals(items, ois);
        assertArrayEquals(scores, oss, 1.0e-5);

        for (int i = 0; i < 3; i++) {
            assertEquals(ni[i], l.getLong(i + 2));
            assertThat(l.getScore(i + 2), notANumber());
        }
    }

    @Test
    public void addScoredElementsShift() {
        long[] items = {42, 1, 2, 5, 37};
        double[] scores = {-1, PI, E, 42, -7};
        ScoredLongList l = new ScoredLongArrayList(items, scores);
        long[] ni = {8, 4, 6};
        double[] ns = {75, -923, 47.8};
        l.addElements(2, ni, ns);
        assertEquals(8, l.size());

        long[] ois = new long[5];
        double[] oss = new double[5];
        l.getElements(0, ois, oss, 0, 2);
        l.getElements(5, ois, oss, 2, 3);
        assertArrayEquals(items, ois);
        assertArrayEquals(scores, oss, 1.0e-5);

        for (int i = 0; i < 3; i++) {
            assertEquals(ni[i], l.getLong(i + 2));
            assertEquals(ns[i], l.getScore(i + 2), 1.0e-5);
        }
    }

    @Test
    public void setScore() {
        long[] items = {1, 2, 5};
        double[] scores = {1.5, 0.3, 7.9};
        ScoredLongArrayList l = new ScoredLongArrayList(items, scores);
        assertEquals(0.3, l.setScore(1, PI), 1.0e-5);
        assertEquals(PI, l.getScore(1), 1.0e-5);
    }

    @Test
    public void setScoreNaN() {
        long[] items = {1, 2, 5};
        ScoredLongArrayList l = new ScoredLongArrayList(items);
        assertThat(l.setScore(1, PI), notANumber());
        assertEquals(PI, l.getScore(1), 1.0e-5);
        assertThat(l.getScore(0), notANumber());
        assertThat(l.getScore(2), notANumber());
    }

    @Test
    public void emptyIterator() {
        ScoredLongList l = new ScoredLongArrayList();
        assertFalse(l.iterator().hasNext());
        assertFalse(l.iterator().hasPrevious());
        assertFalse(l.listIterator().hasNext());
        assertFalse(l.listIterator().hasPrevious());
        try {
            l.iterator().next();
            fail("empty iterator should throw");
        } catch (NoSuchElementException e) {
            /* expected, no-op */
        }
        try {
            l.listIterator().next();
            fail("empty iterator should throw");
        } catch (NoSuchElementException e) {
            /* expected, no-op */
        }
        try {
            l.iterator().previous();
            fail("empty iterator should throw");
        } catch (NoSuchElementException e) {
            /* expected, no-op */
        }
        try {
            l.listIterator().previous();
            fail("empty iterator should throw");
        } catch (NoSuchElementException e) {
            /* expected, no-op */
        }
    }

    @Test
    public void itemIterator() {
        long[] items = {1, 2, 5};
        ScoredLongArrayList l = new ScoredLongArrayList(items);
        assertArrayEquals(items, LongIterators.unwrap(l.iterator()));

        LongListIterator it = l.iterator();
        assertEquals(1, it.nextLong());
        assertEquals(1, it.previousLong());
        assertEquals(1, it.nextLong());
        assertEquals(2, it.nextLong());
        assertTrue(it.hasPrevious());

        assertEquals(2, l.listIterator(2).previousLong());
        assertEquals(5, l.listIterator(2).nextLong());
    }

    @Test
    public void itemScoredIterator() {
        long[] items = {1, 2, 5};
        double[] scores = {7, 42, 9};
        ScoredLongArrayList l = new ScoredLongArrayList(items, scores);
        assertArrayEquals(items, LongIterators.unwrap(l.iterator()));

        ScoredLongListIterator it = l.iterator();
        assertEquals(1, it.nextLong());
        assertEquals(7, it.getScore(), 1.0e-5);
        assertEquals(1, it.previousLong());
        assertEquals(7, it.getScore(), 1.0e-5);
        assertEquals(1, it.nextLong());
        assertEquals(7, it.getScore(), 1.0e-5);
        assertEquals(2, it.nextLong());
        assertEquals(42, it.getScore(), 1.0e-5);
        assertTrue(it.hasPrevious());

        assertEquals(2, l.listIterator(2).previousLong());
        assertEquals(5, l.listIterator(2).nextLong());
    }

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bo);
        long[] items = {1, 2, 5};
        double[] scores = {7, Double.NaN, 9};
        ScoredLongArrayList l = new ScoredLongArrayList(items, scores);
        out.writeObject(l);
        out.close();
        byte[] bytes = bo.toByteArray();

        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bi);
        ScoredLongList l2 = (ScoredLongList) in.readObject();
        in.close();
        assertEquals(l, l2);
    }

    @Test
    public void remove() {
        long[] items = {1, 2, 5};
        double[] scores = {1.5, 0.3, 7.9};
        ScoredLongArrayList l = new ScoredLongArrayList(items, scores);
        assertEquals(2, l.remove(1).longValue());
        assertEquals(2, l.size());
        assertEquals(1, l.getLong(0));
        assertEquals(1.5, l.getScore(0), 1.0e-5);
        assertEquals(5, l.getLong(1));
        assertEquals(7.9, l.getScore(1), 1.0e-5);
    }

    @Test
    public void removeElements() {
        long[] items = {1, 2, 5};
        double[] scores = {1.5, 0.3, 7.9};
        ScoredLongArrayList l = new ScoredLongArrayList(items, scores);
        l.removeElements(0, 2);
        assertEquals(1, l.size());
        assertEquals(5, l.getLong(0));
        assertEquals(7.9, l.getScore(0), 1.0e-5);
    }

    @Test
    public void sort() {
        ScoredLongArrayList l = new ScoredLongArrayList();
        l.add(39L, 4.2);
        l.add(42L, 3.9);
        l.add(1L, 6.4);
        l.add(2L, 1.0);
        l.sort(DoubleComparators.OPPOSITE_COMPARATOR);
        assertEquals(4, l.size());
        assertEquals(1L, l.getLong(0));
        assertEquals(39L, l.getLong(1));
        assertEquals(42L, l.getLong(2));
        assertEquals(2L, l.getLong(3));
        assertEquals(6.4, l.getScore(0), 1.0e-6);
        assertEquals(4.2, l.getScore(1), 1.0e-6);
        assertEquals(3.9, l.getScore(2), 1.0e-6);
        assertEquals(1.0, l.getScore(3), 1.0e-6);
    }
}
