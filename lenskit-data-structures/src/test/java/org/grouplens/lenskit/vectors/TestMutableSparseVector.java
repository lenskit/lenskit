/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import static org.grouplens.common.test.MoreMatchers.closeTo;
import static org.grouplens.common.test.MoreMatchers.notANumber;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;

import java.util.Set;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestMutableSparseVector extends SparseVectorTestCommon {
    @Override
    protected MutableSparseVector emptyVector() {
        return new MutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
    }

    @Override
    protected MutableSparseVector simpleVector() {
        long[] keys = {3, 7, 8};
        double[] values = {1.5, 3.5, 2};
        return MutableSparseVector.wrap(keys, values);
    }

    @Override
    protected MutableSparseVector simpleVector2() {
        long[] keys = {3, 5, 8};
        double[] values = {2, 2.3, 1.7};
        return MutableSparseVector.wrap(keys, values);
    }

    @Override
    protected MutableSparseVector singleton() {
        return MutableSparseVector.wrap(new long[]{5}, new double[]{Math.PI});
    }

    @Test
    public void testCopy() {
        assertTrue(emptyVector().copy().isEmpty());
        MutableSparseVector v1 = singleton();
        MutableSparseVector v2 = v1.copy();
        assertThat(v1, not(sameInstance(v2)));
        assertThat(v1.keySet(), equalTo(v2.keySet()));
        assertThat(v1.values(), equalTo(v2.values()));
        assertThat(v1, equalTo(v2));
        v2.subtract(simpleVector2());
        assertThat(v2.sum(), closeTo(Math.PI - 2.3));
        assertThat(v1.sum(), closeTo(Math.PI));
    }
    
    @Test
    public void testImmutable() {
        MutableSparseVector v = simpleVector();
        ImmutableSparseVector iv = v.immutable();
        v.set(7, 42.0);
        assertThat(v.get(7), closeTo(42.0));
        assertThat(iv.get(7), closeTo(3.5));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testFreeze() {
        MutableSparseVector v = simpleVector();
        ImmutableSparseVector iv = v.freeze();
        assertThat(iv, equalTo((SparseVector) simpleVector()));
        // And this should fail...
        v.get(7);
    }

    @Test
    public void testSubtract() {
        MutableSparseVector v = emptyVector();
        v.subtract(singleton());
        assertTrue(v.isEmpty());

        v = simpleVector2();
        v.subtract(singleton());
        assertThat(v.get(3), closeTo(2));
        assertThat(v.get(5), closeTo(2.3 - Math.PI));
        assertThat(v.get(8), closeTo(1.7));

        v = singleton();
        assertThat(v.sum(), closeTo(Math.PI));
        v.subtract(simpleVector2());
        assertThat(v.get(5), closeTo(Math.PI - 2.3));
        assertThat(v.sum(), closeTo(Math.PI - 2.3));

        v = simpleVector();
        v.subtract(simpleVector2());
        assertThat(v.get(3), closeTo(-0.5));
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(0.3));

        v = simpleVector();
        v.subtract(singleton());
        assertEquals(simpleVector(), v);
    }

    @Test
    public void testAdd() {
        MutableSparseVector v = emptyVector();
        v.add(singleton());
        assertTrue(v.isEmpty());

        v = simpleVector2();
        v.add(singleton());
        assertThat(v.get(3), closeTo(2));
        assertThat(v.get(5), closeTo(2.3 + Math.PI));
        assertThat(v.get(8), closeTo(1.7));

        v = singleton();
        assertThat(v.sum(), closeTo(Math.PI));
        v.add(simpleVector2());
        assertThat(v.get(5), closeTo(Math.PI + 2.3));
        assertThat(v.sum(), closeTo(Math.PI + 2.3));

        v = simpleVector();
        v.add(simpleVector2());
        assertThat(v.get(3), closeTo(3.5));
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(3.7));

        v = simpleVector();
        v.add(singleton());
        assertThat(v, equalTo(simpleVector()));
    }

    @Test
    public void testSetConstructor() {
        long[] keys = {2, 5};
        MutableSparseVector v = new MutableSparseVector(new LongSortedArraySet(keys));
        assertThat(v.size(), equalTo(0));
        assertThat(v.keyDomain().toLongArray(),
                   equalTo(new long[]{2, 5}));
        assertFalse(v.containsKey(2));
        assertTrue(v.keyDomain().contains(2));
        assertThat(v.get(2), notANumber());
    }
    
    @Test
    public void testClear() {
        long[] keys = {2, 5};
        MutableSparseVector v = new MutableSparseVector(new LongSortedArraySet(keys));
        assertThat(v.set(2, Math.PI), notANumber());
        assertThat(v.size(), equalTo(1));
        assertThat(v.get(2), closeTo(Math.PI));
        assertThat(v.containsKey(2), equalTo(true));
        assertThat(v.containsKey(5), equalTo(false));
        assertThat(v.keySet(),
                   equalTo((Set<Long>) Sets.newHashSet(2l)));
        
        v.clear(2);
        assertThat(v.isEmpty(), equalTo(true));
        assertThat(v.size(), equalTo(0));
        assertThat(v.get(2), notANumber());
        assertThat(v.set(2, Math.E), notANumber());
    }


    @Test
    public void testSet() {
        assertThat(emptyVector().set(5, 5), notANumber());
        MutableSparseVector v = simpleVector();
        assertThat(v.set(7, 2), closeTo(3.5));
        assertThat(v.get(7), closeTo(2));
    }

    @Test
    public void testAddToItem() {
        assertThat(emptyVector().add(5, 5), notANumber());
        MutableSparseVector v = simpleVector();
        assertThat(v.add(7, 2), closeTo(5.5));
        assertThat(v.get(7), closeTo(5.5));
        assertThat(v.get(3), closeTo(1.5));
    }

    @Test
    public void testOverSize() {
        long[] keys = {3, 7, 9};
        double[] values = { Math.PI, Math.E, 0.42 };
        MutableSparseVector v = MutableSparseVector.wrap(keys, values, 2);
        assertThat(v.size(), equalTo(2));
        assertThat(v.containsKey(9), equalTo(false));
        assertThat(v.get(9), notANumber());
        assertThat(v.get(3), closeTo(Math.PI));
        v.clear(3);
        assertThat(v.size(), equalTo(1));
        assertArrayEquals(new Long[]{7L}, v.keySet().toArray(new Long[0]));
        assertThat(v.get(7), closeTo(Math.E));
        assertThat(v.set(9, 1.0), notANumber());
        assertThat(v.get(9), notANumber());
        assertThat(v.containsKey(9), equalTo(false));
    }
    
    @Test
    public void testFreezeClear() {
        long[] keys = {3, 7, 9};
        double[] values = { Math.PI, Math.E, 0.42 };
        MutableSparseVector v = MutableSparseVector.wrap(keys, values);
        v.clear(7);
        assertThat(v.size(), equalTo(2));
        ImmutableSparseVector f = v.freeze();
        assertThat(f.size(), equalTo(2));
        assertThat(f.keySet().toLongArray(), equalTo(new long[]{3,9}));
        assertThat(f.get(3), closeTo(Math.PI));
        assertThat(f.get(9), closeTo(0.42));
        assertThat(f.containsKey(7), equalTo(false));
        assertThat(f.get(7), notANumber());
    }
}
