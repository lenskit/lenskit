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
package org.grouplens.lenskit.data.vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.util.LongSortedArraySet;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestSparseVector {
    private static final double EPSILON = 1.0e-6;

    protected MutableSparseVector emptyVector() {
        return new MutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
    }

    /**
     * Construct a simple rating vector with three ratings.
     * @return A rating vector mapping {3, 7, 8} to {1.5, 3.5, 2}.
     */
    protected MutableSparseVector simpleVector() {
        long[] keys = {3, 7, 8};
        double[] values = {1.5, 3.5, 2};
        return MutableSparseVector.wrap(keys, values);
    }

    /**
     * Construct a simple rating vector with three ratings.
     * @return A rating vector mapping {3, 5, 8} to {2, 2.3, 1.7}.
     */
    protected MutableSparseVector simpleVector2() {
        long[] keys = {3, 5, 8};
        double[] values = {2, 2.3, 1.7};
        return MutableSparseVector.wrap(keys, values);
    }


    /**
     * @return A singleton rating vector mapping 5 to PI.
     */
    protected MutableSparseVector singleton() {
        return MutableSparseVector.wrap(new long[]{5}, new double[]{Math.PI});
    }

    protected static void assertIsNaN(double v) {
        if (!Double.isNaN(v))
            fail("Expected NaN, got " + v);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#get(long)}.
     */
    @Test
    public void testGet() {
        assertIsNaN(emptyVector().get(5));
        SparseVector v = singleton();
        assertEquals(Math.PI, v.get(5), EPSILON);
        assertIsNaN(v.get(2));

        v = simpleVector();
        assertEquals(3.5, v.get(7), EPSILON);
        assertEquals(1.5, v.get(3), EPSILON);
        assertEquals(2, v.get(8), EPSILON);
        assertIsNaN(v.get(1));
        assertIsNaN(v.get(4));
        assertIsNaN(v.get(9));
        assertIsNaN(v.get(42));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#get(long, double)}.
     */
    @Test
    public void testGetWithDft() {
        assertIsNaN(emptyVector().get(5, Double.NaN));
        assertEquals(-1, emptyVector().get(5, -1), EPSILON);
        SparseVector v = singleton();
        assertEquals(Math.PI, v.get(5, -1), EPSILON);
        assertEquals(-1, v.get(2, -1), EPSILON);

        v = simpleVector();
        assertEquals(3.5, v.get(7, -1), EPSILON);
        assertEquals(1.5, v.get(3, -1), EPSILON);
        assertEquals(2, v.get(8, -1), EPSILON);
        assertEquals(-1, v.get(1, -1), EPSILON);
        assertEquals(42, v.get(4, 42), EPSILON);
        assertEquals(-7, v.get(9, -7), EPSILON);
        assertEquals(Math.E, v.get(42, Math.E), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#containsKey(long)}.
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
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#iterator()}.
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

        Iterator<Long2DoubleMap.Entry> iter = singleton().iterator();
        assertTrue(iter.hasNext());
        Long2DoubleMap.Entry e = iter.next();
        assertFalse(iter.hasNext());
        assertEquals(5, e.getLongKey());
        assertEquals(Long.valueOf(5), e.getKey());
        assertEquals(Math.PI, e.getDoubleValue(), EPSILON);
        assertEquals(Double.valueOf(Math.PI), e.getValue(), EPSILON);
        try {
            iter.next();
            fail("iter.next() should throw exception");
        } catch (NoSuchElementException x) {
            /* no-op */
        }

        Long2DoubleMap.Entry[] entries = Iterators.toArray(
                simpleVector().iterator(), Long2DoubleMap.Entry.class);
        assertEquals(3, entries.length);
        assertEquals(3, entries[0].getLongKey());
        assertEquals(7, entries[1].getLongKey());
        assertEquals(8, entries[2].getLongKey());
        assertEquals(1.5, entries[0].getDoubleValue(), EPSILON);
        assertEquals(3.5, entries[1].getDoubleValue(), EPSILON);
        assertEquals(2, entries[2].getDoubleValue(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#fastIterator()}.
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

        Iterator<Long2DoubleMap.Entry> iter = singleton().fastIterator();
        assertTrue(iter.hasNext());
        Long2DoubleMap.Entry e = iter.next();
        assertFalse(iter.hasNext());
        assertEquals(5, e.getLongKey());
        assertEquals(Long.valueOf(5), e.getKey());
        assertEquals(Math.PI, e.getDoubleValue(), EPSILON);
        assertEquals(Double.valueOf(Math.PI), e.getValue(), EPSILON);
        try {
            iter.next();
            fail("iter.next() should throw exception");
        } catch (NoSuchElementException x) {
            /* no-op */
        }

        Long[] keys = Iterators.toArray(
                Iterators.transform(simpleVector().fastIterator(),
                        new Function<Long2DoubleMap.Entry,Long>() {
                    @Override
                    public Long apply(Long2DoubleMap.Entry e) {
                        return e.getKey();
                    }
                }), Long.class);
        assertThat(keys, equalTo(new Long[]{3l, 7l, 8l}));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#fast()}.
     */
    @Test
    public void testFast() {
        assertNotNull(emptyVector().fast());
        // TODO: do more testing of the fast() method
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#keySet()}.
     */
    @Test
    public void testIdSet() {
        LongSortedSet set = emptyVector().keySet();
        assertTrue(set.isEmpty());

        long[] keys = singleton().keySet().toLongArray();
        assertThat(keys, equalTo(new long[]{5}));

        keys = simpleVector().keySet().toLongArray();
        assertThat(keys, equalTo(new long[]{3, 7, 8}));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#values()}.
     */
    @Test
    public void testValues() {
        assertTrue(emptyVector().values().isEmpty());

        double[] vals = singleton().values().toDoubleArray();
        assertEquals(1, vals.length);
        assertEquals(Math.PI, vals[0], EPSILON);

        DoubleRBTreeSet s = new DoubleRBTreeSet(simpleVector().values());
        assertThat(s, equalTo(new DoubleRBTreeSet(new double[]{1.5, 3.5, 2})));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#size()}.
     */
    @Test
    public void testSize() {
        assertEquals(0, emptyVector().size());
        assertEquals(1, singleton().size());
        assertEquals(3, simpleVector().size());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#isEmpty()}.
     */
    @Test
    public void testIsEmpty() {
        assertTrue(emptyVector().isEmpty());
        assertFalse(singleton().isEmpty());
        assertFalse(simpleVector().isEmpty());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#norm()}.
     */
    @Test
    public void testNorm() {
        assertEquals(0, emptyVector().norm(), EPSILON);
        assertEquals(Math.PI, singleton().norm(), EPSILON);
        assertEquals(4.301162634, simpleVector().norm(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#sum()}.
     */
    @Test
    public void testSum() {
        assertEquals(0, emptyVector().sum(), EPSILON);
        assertEquals(Math.PI, singleton().sum(), EPSILON);
        assertEquals(7, simpleVector().sum(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#mean()}.
     */
    @Test
    public void testMean() {
        assertEquals(0, emptyVector().mean(), EPSILON);
        assertEquals(Math.PI, singleton().mean(), EPSILON);
        assertEquals(7.0/3, simpleVector().mean(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#copy()}.
     */
    @Test
    public void testCopy() {
        assertTrue(emptyVector().copy().isEmpty());
        MutableSparseVector v1 = singleton();
        MutableSparseVector v2 = v1.copy();
        assertNotSame(v1, v2);
        assertEquals(v1, v2);
        v2.subtract(simpleVector2());
        assertEquals(Math.PI - 2.3, v2.sum(), EPSILON);
        assertEquals(Math.PI, v1.sum(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#clone()}.
     */
    @Test
    public void testClone() {
        assertTrue(emptyVector().clone().isEmpty());
        MutableSparseVector v1 = singleton();
        MutableSparseVector v2 = v1.clone();
        assertNotSame(v1, v2);
        assertEquals(v1, v2);
        v2.subtract(simpleVector2());
        assertEquals(Math.PI - 2.3, v2.sum(), EPSILON);
        assertEquals(Math.PI, v1.sum(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#subtract(org.grouplens.lenskit.data.vector.SparseVector)}.
     */
    @Test
    public void testSubtract() {
        MutableSparseVector v = emptyVector();
        v.subtract(singleton());
        assertTrue(v.isEmpty());

        v = simpleVector2();
        v.subtract(singleton());
        assertEquals(2, v.get(3), EPSILON);
        assertEquals(2.3 - Math.PI, v.get(5), EPSILON);
        assertEquals(1.7, v.get(8), EPSILON);

        v = singleton();
        assertEquals(Math.PI, v.sum(), EPSILON);
        v.subtract(simpleVector2());
        assertEquals(Math.PI - 2.3, v.get(5), EPSILON);
        assertEquals(Math.PI - 2.3, v.sum(), EPSILON);

        v = simpleVector();
        v.subtract(simpleVector2());
        assertEquals(-0.5, v.get(3), EPSILON);
        assertEquals(3.5, v.get(7), EPSILON);
        assertEquals(0.3, v.get(8), EPSILON);

        v = simpleVector();
        v.subtract(singleton());
        assertEquals(simpleVector(), v);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#add(org.grouplens.lenskit.data.vector.SparseVector)}.
     */
    @Test
    public void testAdd() {
        MutableSparseVector v = emptyVector();
        v.add(singleton());
        assertTrue(v.isEmpty());

        v = simpleVector2();
        v.add(singleton());
        assertEquals(2, v.get(3), EPSILON);
        assertEquals(2.3 + Math.PI, v.get(5), EPSILON);
        assertEquals(1.7, v.get(8), EPSILON);

        v = singleton();
        assertEquals(Math.PI, v.sum(), EPSILON);
        v.add(simpleVector2());
        assertEquals(Math.PI + 2.3, v.get(5), EPSILON);
        assertEquals(Math.PI + 2.3, v.sum(), EPSILON);

        v = simpleVector();
        v.add(simpleVector2());
        assertEquals(3.5, v.get(3), EPSILON);
        assertEquals(3.5, v.get(7), EPSILON);
        assertEquals(3.7, v.get(8), EPSILON);

        v = simpleVector();
        v.add(singleton());
        assertEquals(simpleVector(), v);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.vector.MutableSparseVector#dot(org.grouplens.lenskit.data.vector.SparseVector)}.
     */
    @Test
    public void testDot() {
        assertEquals(0, emptyVector().dot(emptyVector()), EPSILON);
        assertEquals(0, emptyVector().dot(simpleVector()), EPSILON);
        assertEquals(0, singleton().dot(simpleVector()), EPSILON);
        assertEquals(0, simpleVector().dot(singleton()), EPSILON);
        assertEquals(6.4, simpleVector().dot(simpleVector2()), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.event.Ratings#userRatingVector(java.util.Collection)}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testUserRatingVector() {
        Collection<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(5, 7, 3.5));
        ratings.add(new SimpleRating(5, 3, 1.5));
        ratings.add(new SimpleRating(5, 8, 2));
        SparseVector v = Ratings.userRatingVector(ratings);
        assertEquals(3, v.size());
        assertEquals(7, v.sum(), EPSILON);
        assertEquals(simpleVector(), v);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.data.event.Ratings#itemRatingVector(java.util.Collection)}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testItemRatingVector() {
        Collection<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(7, 5, 3.5));
        ratings.add(new SimpleRating(3, 5, 1.5));
        ratings.add(new SimpleRating(8, 5, 2));
        SparseVector v = Ratings.itemRatingVector(ratings);
        assertEquals(3, v.size());
        assertEquals(7, v.sum(), EPSILON);
        assertEquals(simpleVector(), v);
    }

    /**
     * Test the set constructor.
     */
    @Test
    public void testSetConstructor() {
        long[] keys = {2, 5};
        MutableSparseVector v = new MutableSparseVector(new LongSortedArraySet(keys));
        assertEquals(2, v.size());
        assertEquals(0, v.get(2), EPSILON);
        assertEquals(0, v.get(5), EPSILON);
    }

    /**
     * Test the set(long, double) method.
     */
    @Test
    public void testSet() {
        assertIsNaN(emptyVector().set(5, 5));
        MutableSparseVector v = simpleVector();
        assertEquals(3.5, v.set(7, 2), EPSILON);
        assertEquals(2, v.get(7), EPSILON);
    }

    /**
     * Test the add(long, double) method.
     */
    @Test
    public void testAddToItem() {
        assertIsNaN(emptyVector().add(5, 5));
        MutableSparseVector v = simpleVector();
        assertEquals(5.5, v.add(7, 2), EPSILON);
        assertEquals(5.5, v.get(7), EPSILON);
        assertEquals(1.5, v.get(3), EPSILON);
    }
    
    @Test
    public void testSortedKeys() {
        assertArrayEquals(new long[]{3,8,7}, simpleVector().keysByValue().toLongArray());
        assertArrayEquals(new long[]{7,8,3}, simpleVector().keysByValue(true).toLongArray());
        assertArrayEquals(new long[]{5,3,8}, simpleVector2().keysByValue(true).toLongArray());
    }
}
