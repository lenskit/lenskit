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
package org.grouplens.lenskit.vectors;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public abstract class SparseVectorTestCommon {
    /**
     * Create an empty vector.
     *
     * @return An empty sparse vector.
     */
    protected abstract SparseVector emptyVector();

    /**
     * @return A singleton rating vector mapping 5 to PI.
     */
    protected abstract SparseVector singleton();

    /**
     * Construct a simple rating vector with three ratings.
     *
     * @return A rating vector mapping {3, 7, 8} to {1.5, 3.5, 2}.
     */
    protected abstract SparseVector simpleVector();

    /**
     * Construct a simple rating vector with three ratings.
     *
     * @return A rating vector mapping {3, 5, 8} to {2, 2.3, 1.7}.
     */
    protected abstract SparseVector simpleVector2();

    public static Matcher<Double> closeTo(double v) {
        return Matchers.closeTo(v, 1.0e-5);
    }

    @Test
    public void testDot() {
        assertThat(emptyVector().dot(emptyVector()), closeTo(0));
        assertThat(emptyVector().dot(simpleVector()), closeTo(0));
        assertThat(singleton().dot(simpleVector()), closeTo(0));
        assertThat(singleton().dot(simpleVector().immutable()), closeTo(0));
        assertThat(simpleVector().dot(singleton()), closeTo(0));
        assertThat(simpleVector().dot(simpleVector2()), closeTo(6.4));
        assertThat(simpleVector().dot(simpleVector2().immutable()), closeTo(6.4));
    }

    @Test
    public void testCountCommonKeys() {
        assertThat(emptyVector().countCommonKeys(emptyVector()),
                   equalTo(0));
        assertThat(emptyVector().countCommonKeys(simpleVector()),
                   equalTo(0));
        assertThat(simpleVector().countCommonKeys(singleton()),
                   equalTo(0));
        assertThat(simpleVector2().countCommonKeys(singleton()),
                   equalTo(1));
        assertThat(simpleVector().countCommonKeys(simpleVector2()),
                   equalTo(2));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#get(long)}.
     */
    @Test
    public void testGet() {
        try {
            emptyVector().get(5);
            fail("invalid key should throw exception");
        } catch (IllegalArgumentException e) { /* expected */ }

        SparseVector v = singleton();
        assertThat(v.get(5), closeTo(Math.PI));
        try {
            v.get(2);
            fail("should throw IllegalArgumentException for bad argument");
        } catch (IllegalArgumentException e) { /* expected */ }

        v = simpleVector();
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(3), closeTo(1.5));
        assertThat(v.get(8), closeTo(2));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#get(long, double)}.
     */
    @Test
    public void testGetWithDft() {
        assertThat(emptyVector().get(5, Double.NaN), notANumber());
        assertThat(emptyVector().get(5, -1), closeTo(-1));
        SparseVector v = singleton();
        assertThat(v.get(5, -1), closeTo(Math.PI));
        assertThat(v.get(2, -1), closeTo(-1));

        v = simpleVector();
        assertThat(v.get(7, -1), closeTo(3.5));
        assertThat(v.get(3, -1), closeTo(1.5));
        assertThat(v.get(8, -1), closeTo(2));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#containsKey(long)}.
     */
    @Test
    public void testContainsId() {
        assertFalse(emptyVector().containsKey(5));
        assertFalse(emptyVector().containsKey(42));
        assertFalse(emptyVector().containsKey(-1));

        assertTrue(singleton().containsKey(5));
        assertFalse(singleton().containsKey(3));
        assertFalse(singleton().containsKey(7));

        assertFalse(simpleVector().containsKey(1));
        assertFalse(simpleVector().containsKey(5));
        assertFalse(simpleVector().containsKey(42));
        assertTrue(simpleVector().containsKey(3));
        assertTrue(simpleVector().containsKey(7));
        assertTrue(simpleVector().containsKey(8));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#iterator()}.
     */
    @Test
    public void testIterator() {
        assertFalse(emptyVector().iterator().hasNext());
        try {
            emptyVector().iterator().next();
            fail("iterator.next() should throw exception");
        } catch (NoSuchElementException x) {
            /* no-op */
        }

        Iterator<VectorEntry> iter = singleton().iterator();
        try {
            iter.remove();
            fail("should throw exception because we cannot remove an item from an iterator on an immutable vector");
        } catch (UnsupportedOperationException x) { /* good*/ }
        assertTrue(iter.hasNext());
        VectorEntry e = iter.next();
        assertFalse(iter.hasNext());
        assertThat(e.getKey(), equalTo(5L));
        assertThat(e.getValue(), closeTo(Math.PI));
        try {
            iter.next();
            fail("iter.next() should throw exception");
        } catch (NoSuchElementException x) {
            /* no-op */
        }

        VectorEntry[] entries =
                Iterators.toArray(simpleVector().iterator(),
                                  VectorEntry.class);
        assertThat(entries.length, equalTo(3));
        assertThat(entries[0].getKey(), equalTo(3L));
        assertThat(entries[1].getKey(), equalTo(7L));
        assertThat(entries[2].getKey(), equalTo(8L));
        assertThat(entries[0].getValue(), closeTo(1.5));
        assertThat(entries[1].getValue(), closeTo(3.5));
        assertThat(entries[2].getValue(), closeTo(2));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#fastIterator()}.
     */
    @Test
    public void testFastIterator() {
        assertFalse(emptyVector().fastIterator().hasNext());
        try {
            emptyVector().fastIterator().next();
            fail("iterator.next() should throw exception");
        } catch (NoSuchElementException x) {
            /* no-op */
        }

        Iterator<VectorEntry> iter = singleton().fastIterator();
        assertTrue(iter.hasNext());
        try {
            iter.remove();
            fail("should throw exception because we cannot remove an item from an iterator on an immutable vector");
        } catch (UnsupportedOperationException x) { /* good*/ }
        VectorEntry e = iter.next();
        assertFalse(iter.hasNext());
        assertThat(e.getKey(), equalTo(5L));
        assertThat(e.getValue(), closeTo(Math.PI));
        try {
            iter.next();
            fail("iter.next() should throw exception");
        } catch (NoSuchElementException x) {
            /* no-op */
        }

        Long[] keys = Iterators.toArray(
                Iterators.transform(simpleVector().fastIterator(),
                                    new Function<VectorEntry, Long>() {
                                        @Override
                                        public Long apply(VectorEntry e) {
                                            return e.getKey();
                                        }
                                    }), Long.class);
        assertThat(keys, equalTo(new Long[]{3l, 7l, 8l}));
    }

    @Test
    public void testFast() {
        assertThat(emptyVector().fast(), notNullValue());
    }

    @Test
    public void testKeysSet() {
        LongSortedSet set = emptyVector().keySet();
        assertTrue(set.isEmpty());

        long[] keys = singleton().keySet().toLongArray();
        assertThat(keys, equalTo(new long[]{5}));

        keys = simpleVector().keySet().toLongArray();
        assertThat(keys, equalTo(new long[]{3, 7, 8}));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#values()}.
     */
    @Test
    public void testValues() {
        assertTrue(emptyVector().values().isEmpty());

        double[] vals = singleton().values().toDoubleArray();
        assertThat(vals.length, equalTo(1));
        assertThat(vals[0], closeTo(Math.PI));

        DoubleRBTreeSet s = new DoubleRBTreeSet(simpleVector().values());
        assertThat(s, equalTo(new DoubleRBTreeSet(new double[]{1.5, 3.5, 2})));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#size()}.
     */
    @Test
    public void testSize() {
        assertThat(emptyVector().size(), equalTo(0));
        assertThat(singleton().size(), equalTo(1));
        assertThat(simpleVector().size(), equalTo(3));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#isEmpty()}.
     */
    @Test
    public void testIsEmpty() {
        assertTrue(emptyVector().isEmpty());
        assertFalse(singleton().isEmpty());
        assertFalse(simpleVector().isEmpty());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.MutableSparseVector#norm()}.
     */
    @Test
    public void testNorm() {
        assertThat(emptyVector().norm(), closeTo(0));
        assertThat(singleton().norm(), closeTo(Math.PI));
        SparseVector sv = simpleVector();
        assertThat(sv.norm(), closeTo(4.301162634));
        assertThat(sv.norm(), closeTo(4.301162634));  // doubled, to check caching
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.SparseVector#sum()}.
     */
    @Test
    public void testSum() {
        assertThat(emptyVector().sum(), closeTo(0));
        assertThat(singleton().sum(), closeTo(Math.PI));
        SparseVector sv = simpleVector();
        assertThat(sv.sum(), closeTo(7));
        assertThat(sv.sum(), closeTo(7)); // doubled, to check caching
    }

    /**
     * Test method for {@link org.grouplens.lenskit.vectors.SparseVector#mean()}.
     */
    @Test
    public void testMean() {
        assertThat(emptyVector().mean(), closeTo(0));
        assertThat(singleton().mean(), closeTo(Math.PI));
        SparseVector sv = simpleVector();
        assertThat(sv.mean(), closeTo(7.0 / 3));
        assertThat(sv.mean(), closeTo(7.0 / 3));  // doubled, to check caching
    }

    @Test
    public void testSortedKeys() {
        assertArrayEquals(new long[]{3, 8, 7}, simpleVector().keysByValue().toLongArray());
        assertArrayEquals(new long[]{7, 8, 3}, simpleVector().keysByValue(true).toLongArray());
        assertArrayEquals(new long[]{5, 3, 8}, simpleVector2().keysByValue(true).toLongArray());
    }

    @Test
    public void testEquals() {
        SparseVector sv = simpleVector();
        assertTrue(sv.equals(sv));
        assertFalse(sv.equals(new VectorEntry(sv, 0, 3, 33, true)));
        assertTrue(simpleVector().equals(simpleVector()));
        assertTrue(singleton().equals(singleton()));
        assertTrue(simpleVector2().equals(simpleVector2()));
        assertTrue(emptyVector().equals(emptyVector()));

        assertFalse(simpleVector().equals(simpleVector2()));
        assertFalse(singleton().equals(simpleVector()));
        assertFalse(simpleVector2().equals(simpleVector()));
        assertFalse(emptyVector().equals(singleton()));
    }

    @Test
    public void testCombine() {
        long[] keys1 = {1, 2, 3};
        double[] values1 = {1.5, 3.5, 2};
        ImmutableSparseVector v1 = MutableSparseVector.wrap(keys1, values1).freeze();
        long[] keys2 = {4};
        double[] values2 = {5};
        ImmutableSparseVector v2 = MutableSparseVector.wrap(keys2, values2).freeze();
        ImmutableSparseVector v = v1.combine(v2);
        assertThat(v.size(), equalTo(4));
        assertThat(v.containsKey(4), equalTo(true));
        assertThat(v.get(4), equalTo(5.0));
        long[] keys3 = {2, 6, 7};
        double[] values3 = {5, 5, 2.5};
        MutableSparseVector v3 = MutableSparseVector.wrap(keys3, values3);
        MutableSparseVector mv = v3.combine(v1);
        assertThat(mv.size(), equalTo(5));
        assertThat(mv.containsKey(1), equalTo(true));
        assertThat(mv.get(2), equalTo(3.5));
    }

    @Test
    public void testVectorEntryMethods() {
        SparseVector simple = simpleVector();
        VectorEntry ve = new VectorEntry(simple, 0, 3, 33, true);
        assertThat(simple.get(3), closeTo(1.5));
        assertThat(ve.getValue(), closeTo(33));  // the VectorEntry is bogus
        assertThat(simple.get(ve), closeTo(1.5));
        assertThat(simple.isSet(ve), equalTo(true));
        
        VectorEntry veBogus = new VectorEntry(null, -1, 3, 33, true);
        try {
            simple.get(veBogus);
            fail("Should throw an IllegalArgumentException because the vector entry has a bogus index");
        } catch (IllegalArgumentException iae) { /* skip */
        }
        try {
            simple.isSet(veBogus);
            fail("Should throw an IllegalArgumentException because the vector entry has a bogus index");
        } catch (IllegalArgumentException iae) { /* skip */
        }

        VectorEntry veNull = new VectorEntry(null, 0, 3, 33, true);
        try {
            simple.get(veNull);
            fail("Should throw an IllegalArgumentException because the vector entry is not attached to this sparse vector");
        } catch (IllegalArgumentException iae) { /* skip */
        }
        try {
            simple.isSet(veNull);
            fail("Should throw an IllegalArgumentException because the vector entry is not attached to this sparse vector");
        } catch (IllegalArgumentException iae) { /* skip */
        }
        
        VectorEntry veBogusKeyDomain = new VectorEntry(simpleVector2(), 0, 3, 1.5, true);
        try {
            simple.get(veBogusKeyDomain);
            fail("Should throw an IllegalArgumentException because the vector entry has a different key domain from the vector");
        } catch (IllegalArgumentException iae) { /* skip */
        }
        try {
            simple.isSet(veBogusKeyDomain);
            fail("Should throw an IllegalArgumentException because the vector entry has a different key domain from the vector");
        } catch (IllegalArgumentException iae) { /* skip */
        }


        MutableSparseVector msv = simpleVector().mutableCopy();
        msv.unset(7);
        VectorEntry veUnset = new VectorEntry(msv, 1, 7, 3.5, false);
        assertThat(msv.isSet(veUnset), equalTo(false));
        try {
            msv.get(veUnset);
            fail("should throw an IllegalArgumentException b/c the vector entry is unset.");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }

}
